/**
 * SkillMatch Core API Client & UI Helpers
 * Handles CSRF tokens, session state, navigation bar, toasts, and dialogs.
 */

const SkillMatch = (function () {
    let currentUser = null;

    function getCookie(name) {
        const value = `; ${document.cookie}`;
        const parts = value.split(`; ${name}=`);
        if (parts.length === 2) return decodeURIComponent(parts.pop().split(';').shift());
        return null;
    }

    async function apiRequest(url, options = {}) {
        const method = options.method || 'GET';
        const headers = options.headers || {};

        // CSRF Protection for state-changing HTTP methods
        if (['POST', 'PUT', 'PATCH', 'DELETE'].includes(method.toUpperCase())) {
            const xsrfToken = getCookie('XSRF-TOKEN');
            if (xsrfToken) {
                headers['X-XSRF-TOKEN'] = xsrfToken;
            }
        }

        if (!(options.body instanceof FormData) && !headers['Content-Type']) {
            headers['Content-Type'] = 'application/json';
        }

        const fetchOptions = {
            method: method,
            headers: headers,
            credentials: 'same-origin', // Include session cookie
            ...options
        };

        try {
            const response = await fetch(url, fetchOptions);

            // Handle 401 Unauthorized
            if (response.status === 401 && !url.includes('/api/auth/login') && !url.includes('/api/auth/me')) {
                window.location.href = '/login.html?session=expired';
                throw new Error('Session expired. Please log in again.');
            }

            // Handle 403 Forbidden
            if (response.status === 403) {
                const data = await response.json().catch(() => ({}));
                const msg = data.message || 'You do not have permission to access this resource.';
                showToast(msg, 'error');
                throw new Error(msg);
            }

            const data = await response.json().catch(() => null);

            if (!response.ok) {
                const errorMsg = data && (data.message || data.error) ? (data.message || data.error) : `Request failed (${response.status})`;
                const err = new Error(errorMsg);
                err.status = response.status;
                err.data = data;
                throw err;
            }

            return data;
        } catch (err) {
            console.error('API Error:', err);
            throw err;
        }
    }

    async function getCurrentUser() {
        if (currentUser) return currentUser;
        try {
            const res = await apiRequest('/api/auth/me');
            if (res && res.success && res.data) {
                currentUser = res.data;
                return currentUser;
            }
        } catch (e) {
            currentUser = null;
        }
        return null;
    }

    async function login(email, password) {
        const res = await apiRequest('/api/auth/login', {
            method: 'POST',
            body: JSON.stringify({ email, password })
        });
        if (res && res.success) {
            currentUser = res.data;
            return currentUser;
        }
        throw new Error((res && res.message) || 'Login failed');
    }

    async function logout() {
        try {
            await apiRequest('/api/auth/logout', { method: 'POST' });
        } catch (e) {
            console.warn('Logout warning:', e);
        }
        currentUser = null;
        window.location.href = '/login.html?loggedOut=true';
    }

    async function requireAuth(allowedRoles = []) {
        const user = await getCurrentUser();
        if (!user) {
            window.location.href = '/login.html?redirect=' + encodeURIComponent(window.location.pathname);
            return null;
        }
        if (allowedRoles.length > 0 && !allowedRoles.includes(user.role)) {
            window.location.href = '/access-denied.html';
            return null;
        }
        return user;
    }

    function initNavbar(activePath = '') {
        const navContainer = document.getElementById('main-navbar');
        if (!navContainer) return;

        getCurrentUser().then(user => {
            let linksHtml = '';
            let actionsHtml = '';

            if (!user) {
                linksHtml = `
                    <li><a href="/opportunities.html" class="nav-link ${activePath === 'opportunities' ? 'active' : ''}">Opportunities</a></li>
                    <li><a href="/index.html#features" class="nav-link">Features</a></li>
                    <li><a href="/index.html#how-it-works" class="nav-link">How It Works</a></li>
                    <li><a href="/index.html#eligibility-simulator" class="nav-link">Simulator</a></li>
                    <li><a href="/index.html#faq" class="nav-link">FAQ</a></li>
                `;
                actionsHtml = `
                    <a href="/login.html" class="btn btn-outline btn-sm">Sign In</a>
                    <a href="/register.html" class="btn btn-primary btn-sm">Join as Student</a>
                `;
            } else if (user.role === 'ROLE_STUDENT') {

                linksHtml = `
                    <li><a href="/student-dashboard.html" class="nav-link ${activePath === 'dashboard' ? 'active' : ''}">Dashboard</a></li>
                    <li><a href="/opportunities.html" class="nav-link ${activePath === 'opportunities' ? 'active' : ''}">Opportunities</a></li>
                    <li><a href="/student-applications.html" class="nav-link ${activePath === 'applications' ? 'active' : ''}">My Applications</a></li>
                    <li><a href="/student-profile.html" class="nav-link ${activePath === 'profile' ? 'active' : ''}">Profile</a></li>
                `;
                actionsHtml = `
                    <span class="user-badge">
                        <span class="user-avatar-circle">${escapeHtml(user.fullName.charAt(0))}</span>
                        ${escapeHtml(user.fullName)}
                    </span>
                    <button onclick="SkillMatch.logout()" class="btn btn-secondary btn-sm" id="btn-logout">Log Out</button>
                `;
            } else if (user.role === 'ROLE_COMPANY') {
                linksHtml = `
                    <li><a href="/company-dashboard.html" class="nav-link ${activePath === 'dashboard' ? 'active' : ''}">Dashboard</a></li>
                    <li><a href="/company-opportunity-form.html" class="nav-link ${activePath === 'post-opp' ? 'active' : ''}">Post Opportunity</a></li>
                    <li><a href="/company-applicants.html" class="nav-link ${activePath === 'applicants' ? 'active' : ''}">Applicants</a></li>
                    <li><a href="/company-profile.html" class="nav-link ${activePath === 'profile' ? 'active' : ''}">Company Profile</a></li>
                `;
                actionsHtml = `
                    <span class="user-badge">
                        <span class="user-avatar-circle">🏢</span>
                        ${escapeHtml(user.fullName)}
                    </span>
                    <button onclick="SkillMatch.logout()" class="btn btn-secondary btn-sm" id="btn-logout">Log Out</button>
                `;
            } else if (user.role === 'ROLE_ADMIN') {
                linksHtml = `
                    <li><a href="/admin-dashboard.html" class="nav-link ${activePath === 'dashboard' ? 'active' : ''}">Overview</a></li>
                    <li><a href="/admin-users.html" class="nav-link ${activePath === 'users' ? 'active' : ''}">Users & Companies</a></li>
                    <li><a href="/admin-opportunities.html" class="nav-link ${activePath === 'opportunities' ? 'active' : ''}">Opportunities</a></li>
                `;
                actionsHtml = `
                    <span class="user-badge" style="background:#fef2f2; border-color:#fecaca; color:#991b1b;">
                        <span class="user-avatar-circle" style="background:#dc2626;">🛡️</span>
                        Administrator
                    </span>
                    <button onclick="SkillMatch.logout()" class="btn btn-secondary btn-sm" id="btn-logout">Log Out</button>
                `;
            }

            navContainer.innerHTML = `
                <div class="nav-container">
                    <a href="/index.html" class="brand-logo">
                        <div class="brand-icon">⚡</div>
                        SkillMatch
                    </a>
                    <ul class="nav-links">
                        ${linksHtml}
                    </ul>
                    <div class="nav-actions">
                        ${actionsHtml}
                    </div>
                    <button class="mobile-menu-toggle" id="mobile-menu-btn" aria-label="Toggle navigation menu" onclick="SkillMatch.toggleMobileMenu()">
                        <span class="hamburger-bar"></span>
                        <span class="hamburger-bar"></span>
                        <span class="hamburger-bar"></span>
                    </button>
                </div>
                <div class="mobile-nav-drawer" id="mobile-nav-drawer">
                    <ul class="mobile-nav-links">
                        ${linksHtml}
                    </ul>
                    <div class="mobile-nav-actions">
                        ${actionsHtml}
                    </div>
                </div>
            `;
        });
    }

    function toggleMobileMenu() {
        const drawer = document.getElementById('mobile-nav-drawer');
        const btn = document.getElementById('mobile-menu-btn');
        if (drawer && btn) {
            drawer.classList.toggle('open');
            btn.classList.toggle('open');
        }
    }

    function showToast(message, type = 'info') {
        let container = document.getElementById('toast-container');
        if (!container) {
            container = document.createElement('div');
            container.id = 'toast-container';
            document.body.appendChild(container);
        }

        const toast = document.createElement('div');
        toast.className = `toast ${type}`;
        toast.innerHTML = `
            <span>${escapeHtml(message)}</span>
            <button style="background:none;border:none;cursor:pointer;font-weight:bold;color:#64748b;" onclick="this.parentElement.remove()">×</button>
        `;
        container.appendChild(toast);

        setTimeout(() => {
            if (toast.parentElement) {
                toast.style.opacity = '0';
                toast.style.transform = 'translateX(100%)';
                setTimeout(() => toast.remove(), 300);
            }
        }, 4500);
    }

    function showConfirm(title, message, onConfirm) {
        let overlay = document.getElementById('confirm-modal-overlay');
        if (!overlay) {
            overlay = document.createElement('div');
            overlay.id = 'confirm-modal-overlay';
            overlay.className = 'modal-overlay';
            overlay.innerHTML = `
                <div class="modal-content" style="max-width:440px; text-align:center;">
                    <h3 id="confirm-title" style="margin-bottom:0.75rem;">Confirm Action</h3>
                    <p id="confirm-message" style="color:var(--surface-500); margin-bottom:1.5rem;"></p>
                    <div style="display:flex; justify-content:center; gap:0.75rem;">
                        <button class="btn btn-secondary" id="confirm-cancel-btn">Cancel</button>
                        <button class="btn btn-danger" id="confirm-ok-btn">Confirm</button>
                    </div>
                </div>
            `;
            document.body.appendChild(overlay);
        }

        document.getElementById('confirm-title').textContent = title;
        document.getElementById('confirm-message').textContent = message;

        const cancelBtn = document.getElementById('confirm-cancel-btn');
        const okBtn = document.getElementById('confirm-ok-btn');

        const close = () => { overlay.classList.remove('active'); };
        cancelBtn.onclick = close;
        overlay.onclick = (e) => { if (e.target === overlay) close(); };

        okBtn.onclick = () => {
            close();
            if (typeof onConfirm === 'function') onConfirm();
        };

        overlay.classList.add('active');
    }

    function formatStatusBadge(status) {
        if (!status) return '';
        const labels = {
            'APPLIED': 'Applied',
            'UNDER_REVIEW': 'Under Review',
            'SHORTLISTED': 'Shortlisted',
            'INTERVIEW_SCHEDULED': 'Interview Scheduled',
            'SELECTED': 'Selected',
            'REJECTED': 'Rejected',
            'PUBLISHED': 'Published',
            'CLOSED': 'Closed',
            'DRAFT': 'Draft'
        };
        const label = labels[status] || status;
        return `<span class="badge badge-${status}"><span class="badge-dot"></span>${escapeHtml(label)}</span>`;
    }

    function formatDate(dateStr) {
        if (!dateStr) return 'N/A';
        try {
            const d = new Date(dateStr);
            return d.toLocaleDateString('en-US', { year: 'numeric', month: 'short', day: 'numeric' });
        } catch (e) {
            return dateStr;
        }
    }

    function escapeHtml(str) {
        if (str === null || str === undefined) return '';
        return String(str)
            .replace(/&/g, '&amp;')
            .replace(/</g, '&lt;')
            .replace(/>/g, '&gt;')
            .replace(/"/g, '&quot;')
            .replace(/'/g, '&#039;');
    }

    return {
        apiRequest,
        getCurrentUser,
        login,
        logout,
        requireAuth,
        initNavbar,
        toggleMobileMenu,
        showToast,
        showConfirm,
        formatStatusBadge,
        formatDate,
        escapeHtml
    };
})();
