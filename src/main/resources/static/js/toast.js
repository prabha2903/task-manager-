// ============================================================
// toast.js — Toast notification utility
// ============================================================

const Toast = {
  container: null,

  init() {
    if (!this.container) {
      this.container = document.createElement('div');
      this.container.id = 'toast-container';
      document.body.appendChild(this.container);
    }
  },

  show(message, type = 'info', title = null, duration = 4000) {
    this.init();

    const icons = {
      info: '🔔',
      success: '✅',
      error: '❌',
      warning: '⚠️',
    };

    const titles = {
      info: 'Notification',
      success: 'Success',
      error: 'Error',
      warning: 'Warning',
    };

    const toast = document.createElement('div');
    toast.className = 'toast-notif';
    toast.innerHTML = `
      <div class="toast-icon ${type}">${icons[type] || icons.info}</div>
      <div class="toast-content">
        <div class="toast-title">${title || titles[type]}</div>
        <div class="toast-message">${message}</div>
      </div>
    `;

    this.container.appendChild(toast);

    setTimeout(() => {
      toast.classList.add('fade-out');
      setTimeout(() => toast.remove(), 300);
    }, duration);
  },

  success(message, title) { this.show(message, 'success', title); },
  error(message, title) { this.show(message, 'error', title); },
  warning(message, title) { this.show(message, 'warning', title); },
  info(message, title) { this.show(message, 'info', title); },
};

window.Toast = Toast;