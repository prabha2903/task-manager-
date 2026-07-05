const Theme = {

  init() {

    const saved = localStorage.getItem('theme') || 'dark';

    document.documentElement.setAttribute('data-theme', saved);

    this.bindToggle();

    this.updateIcon(saved);
  },

  bindToggle() {

  const btn = document.getElementById('theme-toggle');

  if (!btn) {
    console.warn('[Theme] Toggle button not found');
    return;
  }

  // prevent duplicate listeners
  if (btn.dataset.bound === 'true') {
    return;
  }

  btn.dataset.bound = 'true';

  btn.addEventListener('click', () => {
    this.toggle();
  });
},

  toggle() {

    const current =
      document.documentElement.getAttribute('data-theme') || 'dark';

    const next = current === 'dark'
      ? 'light'
      : 'dark';

    document.documentElement.setAttribute('data-theme', next);

    localStorage.setItem('theme', next);

    this.updateIcon(next);

    console.log('[Theme] Switched to:', next);
  },

  updateIcon(theme) {

    const btn = document.getElementById('theme-toggle');

    if (!btn) return;

    btn.textContent =
      theme === 'dark'
        ? '🌙'
        : '☀️';
  }
};

window.Theme = Theme;