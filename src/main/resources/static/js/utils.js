// ============================================================
// utils.js — Shared utilities, sidebar, topbar, auth guards
// ============================================================

// -----------------------------------------------------------
// Auth Guard
// -----------------------------------------------------------
function requireAuth() {
  if (!Api.isAuthenticated()) {
    window.location.href = 'index.html';
    return false;
  }
  return true;
}

function redirectIfAuth() {
  if (Api.isAuthenticated()) {
    window.location.href = 'dashboard.html';
  }
}

// -----------------------------------------------------------
// Sidebar HTML generator
// -----------------------------------------------------------
function renderSidebar(activePage) {
  const user = Api.getUser();
  const name = (user && user.name) ? user.name : 'User';
  const initials = name.split(' ').map(function(w){ return w[0]; }).join('').toUpperCase().slice(0,2);
  const role = (user.role?.name || user.role || 'developer')
  .toString()
  .toLowerCase();

  var navItems = [
    { page: 'dashboard', icon: '🏠', label: 'Dashboard', href: 'dashboard.html' },
    { page: 'tasks',     icon: '✅', label: 'Tasks',     href: 'tasks.html' },
    { page: 'kanban',    icon: '📋', label: 'Kanban',    href: 'kanban.html' },
    { page: 'users',     icon: '👥', label: 'Team',      href: 'users.html' },
    { page: 'projects',  icon: '📁', label: 'Projects',  href: 'projects.html' },
    { page: 'reports', icon: '📈', label: 'Reports', href: 'reports.html' }
  ];

  var navHTML = navItems.map(function(item) {
    return '<li class="nav-item">' +
      '<a href="' + item.href + '" class="nav-link ' + (activePage === item.page ? 'active' : '') + '">' +
        '<span class="nav-icon">' + item.icon + '</span>' + item.label +
      '</a></li>';
  }).join('');

  return [
    '<div class="sidebar" id="sidebar">',
    '  <div class="sidebar-logo">',
    '    <div class="logo-icon">⚡</div>',
    '    <span class="logo-text">TaskFlow</span>',
    '  </div>',
    '  <div class="sidebar-section">',
    '    <div class="sidebar-section-label">Navigation</div>',
    '    <ul class="nav flex-column" style="list-style:none;padding:0;margin:0">' + navHTML + '</ul>',
    '  </div>',
    '  <div class="sidebar-footer">',
    '    <div class="user-profile">',
    '      <div class="user-avatar">' + initials + '</div>',
    '      <div class="user-info">',
    '        <div class="user-name">' + _esc(name) + '</div>',
    '        <div class="user-role">' + _esc(role) + '</div>',
    '      </div>',
    '    </div>',
    '    <button class="btn btn-danger btn-sm w-100 mt-2" id="logout-btn">🚪 Logout</button>',
    '  </div>',
    '</div>'
  ].join('');
}

// -----------------------------------------------------------
// Topbar HTML generator
// -----------------------------------------------------------
function renderTopbar(title, actions) {
  actions = actions || '';
  return [
    '<div class="topbar">',
    '  <button class="btn btn-secondary btn-icon sidebar-toggle-btn" id="sidebar-toggle" style="display:none">☰</button>',
    '  <h1 class="topbar-title">' + title + '</h1>',
    '  <div class="topbar-actions">',
     actions,
'    <button id="theme-toggle" class="btn btn-secondary btn-icon" title="Toggle Theme">🌙</button>',
     '<div class="shortcut-hint">⌨ N • K • / • ESC</div>',
'    <div style="position:relative">',
    '      <button class="notif-btn" id="notif-btn" title="Notifications">',
    '        🔔',
    '        <span class="notif-badge" id="notif-badge">0</span>',
    '      </button>',
    '      <div class="notif-dropdown" id="notif-dropdown">',
    '        <div class="notif-header">',
    '          <h6 style="margin:0">Notifications</h6>',
    '          <button class="btn btn-secondary btn-sm" onclick="Notifications.markAllRead()">Mark all read</button>',
    '        </div>',
    '        <div class="notif-list" id="notif-list">',
    '          <div class="notif-empty">🔕 No notifications yet</div>',
    '        </div>',
    '      </div>',
    '    </div>',
    '  </div>',
    '</div>'
  ].join('');
}

// -----------------------------------------------------------
// Main page initializer — call from every protected page
// -----------------------------------------------------------
function initPage(activePage, title, topbarActions) {
  if (!requireAuth()) return false;

  var sidebarMount = document.getElementById('sidebar-mount');
  if (sidebarMount) sidebarMount.innerHTML = renderSidebar(activePage);

  var topbarMount = document.getElementById('topbar-mount');
  if (topbarMount) topbarMount.innerHTML = renderTopbar(title, topbarActions || '');

  // Show mobile toggle on small screens
  function checkMobile() {
    var btn = document.getElementById('sidebar-toggle');
    if (btn) btn.style.display = window.innerWidth < 768 ? 'flex' : 'none';
  }
  checkMobile();
  window.addEventListener('resize', checkMobile);

  // Mobile sidebar toggle
  document.addEventListener('click', function(e) {
    if (e.target && (e.target.id === 'sidebar-toggle' || e.target.closest && e.target.closest('#sidebar-toggle'))) {
      var sb = document.getElementById('sidebar');
      if (sb) sb.classList.toggle('mobile-open');
    }
    // Close sidebar when clicking outside on mobile
    var sb = document.getElementById('sidebar');
    if (sb && sb.classList.contains('mobile-open')) {
      if (!sb.contains(e.target) && e.target.id !== 'sidebar-toggle') {
        sb.classList.remove('mobile-open');
      }
    }
  });

  // Logout
  document.addEventListener('click', function(e) {
    if (e.target && e.target.id === 'logout-btn') {
      handleLogout();
    }
  });

  // Init notifications WebSocket
  Notifications.init();
   // ── Theme: retry until theme.js finishes async loading ──────────
  (function tryTheme(n) {
    if (window.Theme && typeof window.Theme.init === 'function') {

      if (!window.Theme.__pageInit) {
        window.Theme.__pageInit = true;
        window.Theme.init();
      }

    } else if (n > 0) {

      setTimeout(function () {
        tryTheme(n - 1);
      }, 50);

    }
  })(40);
  return true;
}

function handleLogout() {
  if (confirm('Are you sure you want to logout?')) {
    Notifications.disconnect();
    Api.removeToken();
    window.location.href = 'index.html';
  }
}

// -----------------------------------------------------------
// Badge helpers
// -----------------------------------------------------------
function statusBadge(status) {
  var map = {
    'BACKLOG':     '<span class="badge badge-backlog">Backlog</span>',
    'IN_PROGRESS': '<span class="badge badge-inprogress">In Progress</span>',
    'REVIEW':      '<span class="badge badge-review">Review</span>',
    'QA':          '<span class="badge badge-qa">QA</span>',
    'BLOCKED':     '<span class="badge badge-blocked">Blocked</span>',
    'DONE':        '<span class="badge badge-done">Done</span>',
  };

  return map[status] ||
    '<span class="badge badge-todo">' + _esc(status) + '</span>';
}

function priorityBadge(priority) {
  var map = {
    'LOW':    '<span class="badge badge-low">Low</span>',
    'MEDIUM': '<span class="badge badge-medium">Medium</span>',
    'HIGH':   '<span class="badge badge-high">High</span>',
  };
  return map[priority] || '<span class="badge badge-todo">' + _esc(priority) + '</span>';
}

function roleBadge(role) {
  var r = (role || '').toLowerCase();
  return '<span class="role-badge ' + r + '">' + _esc(role || 'User') + '</span>';
}

// -----------------------------------------------------------
// Date helpers
// -----------------------------------------------------------
function formatDate(iso) {
  if (!iso) return '—';
  var d = new Date(iso);
  if (isNaN(d.getTime())) return String(iso);
  return d.toLocaleDateString('en-US', { month: 'short', day: 'numeric', year: 'numeric' });
}

function isOverdue(iso) {
  if (!iso) return false;
  var d = new Date(iso);
  var now = new Date();
  return d < now && d.toDateString() !== now.toDateString();
}

function timeAgo(iso) {
  if (!iso) return '';
  var diff = Date.now() - new Date(iso).getTime();
  if (diff < 60000) return 'just now';
  if (diff < 3600000) return Math.floor(diff / 60000) + 'm ago';
  if (diff < 86400000) return Math.floor(diff / 3600000) + 'h ago';
  return new Date(iso).toLocaleDateString();
}

// -----------------------------------------------------------
// Utility helpers
// -----------------------------------------------------------
function _esc(s) {
  return String(s || '')
    .replace(/&/g, '&amp;')
    .replace(/</g, '&lt;')
    .replace(/>/g, '&gt;')
    .replace(/"/g, '&quot;');
}

function confirmAction(msg) {
  return confirm(msg);
}

function debounce(fn, delay) {
  delay = delay || 300;
  var timer;
  return function() {
    var args = arguments;
    clearTimeout(timer);
    timer = setTimeout(function() { fn.apply(null, args); }, delay);
  };
}

function getInitials(name) {
  return String(name || 'U').split(' ').map(function(w){ return w[0]; }).join('').toUpperCase().slice(0,2);
}
 
// ======================================================
// Keyboard Shortcuts
// N = New Task
// K = Kanban
// / = Focus Search
// ESC = Close Modal
// ======================================================

// ======================================================
// Keyboard Shortcuts
// N = New Task
// K = Kanban
// / = Focus Search
// ESC = Close Modal
// ======================================================

document.addEventListener('keydown', function(e) {

  // Don't trigger while typing
  const tag = document.activeElement.tagName;

  if (
    tag === 'INPUT' ||
    tag === 'TEXTAREA' ||
    document.activeElement.isContentEditable
  ) {
    // Allow ESC even inside input
    if (e.key !== 'Escape') {
      return;
    }
  }

  // ====================================================
  // N → Open New Task Modal
  // ====================================================

  if (e.key === 'n' || e.key === 'N') {

    e.preventDefault();

    // tasks page la modal open
    if (
      window.TasksPage &&
      typeof TasksPage.openCreateModal === 'function'
    ) {
      TasksPage.openCreateModal();
      return;
    }

   // fallback
window.location.href = 'tasks.html?new=true';
  }

  // ====================================================
  // K → Open Kanban
  // ====================================================

  if (e.key === 'k' || e.key === 'K') {

    e.preventDefault();

    window.location.href = 'kanban.html';
  }

  // ====================================================
  // / → Focus Search
  // ====================================================

  if (e.key === '/') {

    e.preventDefault();

    const searchInput =
      document.getElementById('filter-search');

    if (searchInput) {
      searchInput.focus();
    }
  }

  // ====================================================
  // ESC → Close Modal
  // ====================================================

  if (e.key === 'Escape') {

    const modal =
      document.querySelector('.modal.show');

    if (modal && window.bootstrap) {

      const bsModal =
        bootstrap.Modal.getInstance(modal);

      if (bsModal) {
        bsModal.hide();
      }
    }
  }

});
// Expose everything globally
window.requireAuth    = requireAuth;
window.redirectIfAuth = redirectIfAuth;
window.initPage       = initPage;
window.handleLogout   = handleLogout;
window.statusBadge    = statusBadge;
window.priorityBadge  = priorityBadge;
window.roleBadge      = roleBadge;
window.formatDate     = formatDate;
window.isOverdue      = isOverdue;
window.timeAgo        = timeAgo;
window.confirmAction  = confirmAction;
window.debounce       = debounce;
window.getInitials    = getInitials;
window._esc           = _esc;