// ============================================================
// activity.js — Activity log renderer for task detail page
// ============================================================

const Activity = {
  async load(taskId, containerId = 'activity-timeline') {
    const container = document.getElementById(containerId);
    if (!container) return;

    container.innerHTML = `
      <div class="loading-spinner">
        <div class="spinner"></div> Loading activity...
      </div>`;

    const data = await Api.getActivityLog(taskId);

    if (!data || !Array.isArray(data) || data.length === 0) {
      container.innerHTML = `
        <div class="empty-state" style="padding:30px">
          <div class="empty-state-icon">📋</div>
          <h6>No activity yet</h6>
          <p>Actions on this task will appear here.</p>
        </div>`;
      return;
    }

    container.innerHTML = `<div class="activity-timeline">${data.map(log => this._renderItem(log)).join('')}</div>`;
  },

  _renderItem(log) {
    const icon = this._getIcon(log.action);
    const actionText = this._formatAction(log);
    const time = this._timeAgo(log.createdAt || log.timestamp);

    return `
      <div class="activity-item">
        <div class="activity-dot">${icon}</div>
        <div class="activity-content">
          <div class="activity-text">${actionText}</div>
          <div class="activity-time">${time}</div>
        </div>
      </div>`;
  },

  _formatAction(log) {
    const user = `<strong>${this._esc(log.performedBy || log.user || 'Unknown')}</strong>`;
    const action = log.action || '';
    const detail = log.detail || log.newValue || '';

    const templates = {
      'TASK_CREATED': `${user} created this task`,
      'STATUS_CHANGED': `${user} changed status to <span class="badge badge-${(detail || '').toLowerCase().replace(' ', '')}">${this._esc(detail)}</span>`,
      'ASSIGNED': `${user} assigned the task to <strong>${this._esc(detail)}</strong>`,
      'COMMENT_ADDED': `${user} added a comment`,
      'PRIORITY_CHANGED': `${user} changed priority to <strong>${this._esc(detail)}</strong>`,
      'TASK_UPDATED': `${user} updated the task`,
      'DUE_DATE_CHANGED': `${user} changed due date to <strong>${this._esc(detail)}</strong>`,
    };

    return templates[action] || `${user} performed action: <em>${this._esc(action)}</em>${detail ? ` → ${this._esc(detail)}` : ''}`;
  },

  _getIcon(action) {
    const icons = {
      'TASK_CREATED': '✨',
      'STATUS_CHANGED': '🔄',
      'ASSIGNED': '👤',
      'COMMENT_ADDED': '💬',
      'PRIORITY_CHANGED': '🚩',
      'TASK_UPDATED': '✏️',
      'DUE_DATE_CHANGED': '📅',
    };
    return icons[action] || '📌';
  },

  _timeAgo(iso) {
    if (!iso) return 'Unknown time';
    const diff = Date.now() - new Date(iso).getTime();
    if (diff < 60000) return 'Just now';
    if (diff < 3600000) return `${Math.floor(diff / 60000)} mins ago`;
    if (diff < 86400000) return `${Math.floor(diff / 3600000)} hours ago`;
    if (diff < 604800000) return `${Math.floor(diff / 86400000)} days ago`;
    return new Date(iso).toLocaleDateString();
  },

  _esc(str) {
    return String(str || '')
      .replace(/&/g, '&amp;')
      .replace(/</g, '&lt;')
      .replace(/>/g, '&gt;');
  },
};

window.Activity = Activity;