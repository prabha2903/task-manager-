// ============================================================
// tasks.js — Task list page logic with filters + pagination
// ============================================================
var allTasks = [];
const TasksPage = {
  
  page: 0,
  size: 10,
  totalPages: 0,
  totalElements: 0,
  filters: { title: '', status: '', priority: '', assignedUserId: '' },
  users: [],
  projects: [],
  currentEditId: null,

  async init() {
    await this.loadUsers();
    await this.loadProjects();
    await this.loadTasks();
    this.bindEvents();
  },

  bindEvents() {
    // Search with debounce
    const searchInput = document.getElementById('filter-title');
    if (searchInput) {
      searchInput.addEventListener('input', debounce(() => {
        this.filters.title = searchInput.value.trim();
        this.page = 0;
        this.loadTasks();
      }, 400));
    }

    // Dropdowns
    ['filter-status', 'filter-priority', 'filter-user'].forEach(id => {
      const el = document.getElementById(id);
      if (el) {
        el.addEventListener('change', () => {
          if (id === 'filter-status') this.filters.status = el.value;
          if (id === 'filter-priority') this.filters.priority = el.value;
          if (id === 'filter-user') this.filters.assignedUserId = el.value;
          this.page = 0;
          this.loadTasks();
        });
      }
    });

    // Create task form submit
   // Save Task Button
document.getElementById('save-task-btn')
  ?.addEventListener('click', () => this.saveTask());
    document.getElementById('new-task-btn')?.addEventListener('click', () => {
  this.openCreateModal();
});
    // Modal reset on open
    const modal = document.getElementById('task-modal');
    if (modal) {
      modal.addEventListener('show.bs.modal', () => {});
    }
  },

  async loadUsers() {
    const data = await Api.getUsers();
    this.users = data || [];
    const sel = document.getElementById('filter-user');
    const formSel = document.getElementById('form-assignee');
    const opts = this.users.map(u => `<option value="${u.id}">${escHtml(u.name)}</option>`).join('');
    if (sel) sel.innerHTML = `<option value="">All Users</option>${opts}`;
    if (formSel) formSel.innerHTML = `<option value="">Unassigned</option>${opts}`;
  },
    async loadProjects() {
  const data = await Api.getProjects();

  this.projects = data?.content || data || [];

  const sel = document.getElementById('form-project');

  if (sel) {
    sel.innerHTML =
      `<option value="">Select Project</option>` +
      this.projects.map(p =>
        `<option value="${p.id}">${escHtml(p.name)}</option>`
      ).join('');
  }
},
  buildQueryParams() {
    const params = { page: this.page, size: this.size };
    if (this.filters.title) params.keyword = this.filters.title;
    if (this.filters.status) params.status = this.filters.status;
    if (this.filters.priority) params.priority = this.filters.priority;
    if (this.filters.assignedUserId) params.assignedUserId = this.filters.assignedUserId;
    return params;
  },

  async loadTasks() {
    const tbody = document.getElementById('tasks-tbody');
    if (!tbody) return;

    tbody.innerHTML = `<tr><td colspan="7" class="text-center" style="padding:40px">
      <div class="loading-spinner" style="justify-content:center"><div class="spinner"></div> Loading tasks...</div>
    </td></tr>`;

    const data = await Api.searchTasks(this.buildQueryParams());
    if (!data) {
      tbody.innerHTML = `<tr><td colspan="7"><div class="empty-state"><div class="empty-state-icon">❌</div><h6>Failed to load</h6></div></td></tr>`;
      return;
    }

    // Handle both paginated and array responses
    const tasks = data.content || (Array.isArray(data) ? data : []);
    allTasks = tasks;
    this.totalPages = data.totalPages || 1;
    this.totalElements = data.totalElements || tasks.length;

    if (!tasks.length) {
      tbody.innerHTML = `<tr><td colspan="7">
        <div class="empty-state">
          <div class="empty-state-icon">📭</div>
          <h6>No tasks found</h6>
          <p>Try adjusting your filters or create a new task.</p>
        </div>
      </td></tr>`;
    } else {
      tbody.innerHTML = tasks.map(t => this.renderRow(t)).join('');
    }

    this.renderPagination();
    console.log("QUERY:", this.buildQueryParams());
console.log("TOKEN:", Api.getToken());
  },

  renderRow(t) {
  const user = Api.getUser();
  const isAdmin = user?.role === 'ADMIN'

  const overdue = isOverdue(t.dueDate);

  return `
    <tr>
      <td>
        <a href="task-detail.html?id=${t.id}" 
           style="color:var(--text-primary);text-decoration:none;font-weight:500;
                  display:block;max-width:220px;overflow:hidden;text-overflow:ellipsis;white-space:nowrap"
           title="${escHtml(t.title)}">
          ${escHtml(t.title)}
        </a>
      </td>

      <td>${statusBadge(t.status)}</td>
      <td>${priorityBadge(t.priority)}</td>

      <td style="color:var(--text-secondary);font-size:13px">
        ${escHtml(t.assignedUser?.name || t.assignedTo?.name || '—')}
      </td>

      <td style="font-size:13px;color:var(--text-muted)">
        ${escHtml(t.project?.name || '—')}
      </td>

      <td>
        <span style="font-size:12px;${overdue ? 'color:var(--accent-red);font-weight:600' : 'color:var(--text-muted)'}">
          ${overdue ? '⚠️ ' : ''}${formatDate(t.dueDate)}
        </span>
      </td>

      <td>
        <div style="display:flex;gap:6px">
          <a href="task-detail.html?id=${t.id}" class="btn btn-secondary btn-sm" title="View">👁</a>

          <button class="btn btn-secondary btn-sm"
            onclick="TasksPage.openEditModal(${t.id})"
            title="Edit">✏️</button>

          ${isAdmin ? `
            <button class="btn btn-danger btn-sm"
              onclick="TasksPage.deleteTask(${t.id})"
              title="Delete">🗑</button>
          ` : ''}
        </div>
      </td>
    </tr>
  `;
},

  renderPagination() {
    const wrap = document.getElementById('pagination-wrap');
    if (!wrap) return;

    const from = this.totalElements === 0 ? 0 : this.page * this.size + 1;
    const to = Math.min((this.page + 1) * this.size, this.totalElements);

    // Build page buttons (show max 5 around current)
    let pageButtons = '';
    const maxVisible = 5;
    let startPage = Math.max(0, this.page - 2);
    let endPage = Math.min(this.totalPages - 1, startPage + maxVisible - 1);
    if (endPage - startPage < maxVisible - 1) startPage = Math.max(0, endPage - maxVisible + 1);

    for (let i = startPage; i <= endPage; i++) {
      pageButtons += `<button class="page-btn ${i === this.page ? 'active' : ''}" onclick="TasksPage.goToPage(${i})">${i + 1}</button>`;
    }

    wrap.innerHTML = `
      <div class="pagination-info">Showing ${from}–${to} of ${this.totalElements} tasks</div>
      <div class="pagination-controls">
        <button class="page-btn" onclick="TasksPage.goToPage(0)" ${this.page === 0 ? 'disabled' : ''} title="First">«</button>
        <button class="page-btn" onclick="TasksPage.goToPage(${this.page - 1})" ${this.page === 0 ? 'disabled' : ''} title="Previous">‹</button>
        ${pageButtons}
        <button class="page-btn" onclick="TasksPage.goToPage(${this.page + 1})" ${this.page >= this.totalPages - 1 ? 'disabled' : ''} title="Next">›</button>
        <button class="page-btn" onclick="TasksPage.goToPage(${this.totalPages - 1})" ${this.page >= this.totalPages - 1 ? 'disabled' : ''} title="Last">»</button>
      </div>`;
  },

  goToPage(p) {
    if (p < 0 || p >= this.totalPages) return;
    this.page = p;
    this.loadTasks();
  },

  openCreateModal() {
    this.currentEditId = null;
    document.getElementById('modal-title').textContent = '+ New Task';
    document.getElementById('form-id').value = '';
    const modal = new bootstrap.Modal(document.getElementById('task-modal'));
    modal.show();
  },

  async openEditModal(id) {
    this.currentEditId = id;
    document.getElementById('modal-title').textContent = 'Edit Task';

    const task = await Api.getTask(id);
    if (!task) return;

    document.getElementById('form-id').value = task.id;
    document.getElementById('form-title').value = task.title || '';
    document.getElementById('form-description').value = task.description || '';
    document.getElementById('form-priority').value = task.priority || 'MEDIUM';
    document.getElementById('form-status').value = task.status || 'TODO';
    document.getElementById('form-duedate').value = task.dueDate ? task.dueDate.split('T')[0] : '';
    const assignSel = document.getElementById('form-assignee');
    if (assignSel) assignSel.value = task.assignedUser?.id || task.assignedTo?.id || '';

    const modal = new bootstrap.Modal(document.getElementById('task-modal'));
    const projSel = document.getElementById('form-project');
if (projSel) {
  projSel.value = task.project?.id || '';
}
    modal.show();
  },

  async saveTask() {
    
    const id = document.getElementById('form-id').value;
    const payload = {
      title: document.getElementById('form-title').value.trim(),
      description: document.getElementById('form-description').value.trim(),
      priority: document.getElementById('form-priority').value,
      status: document.getElementById('form-status').value || 'BACKLOG',
      dueDate: document.getElementById('form-duedate').value || null,
assignedUserId: document.getElementById('form-assignee').value
  ? Number(document.getElementById('form-assignee').value)
  : null,

projectId: document.getElementById('form-project').value
  ? Number(document.getElementById('form-project').value)
  : null,
    };

    if (!payload.title) {
      Toast.error('Task title is required.');
      return;
    }

    const btn = document.getElementById('save-task-btn');
    btn.disabled = true;
    btn.textContent = 'Saving...';

    let result;
    if (id) {
      result = await Api.updateTask(id, payload);
    } else {
      result = await Api.createTask(payload);
    }

    btn.disabled = false;
    btn.textContent = 'Save Task';

    if (result) {
      Toast.success(id ? 'Task updated!' : 'Task created!');
      const modalEl = document.getElementById('task-modal');
const modalInstance = bootstrap.Modal.getInstance(modalEl);
if (modalInstance) {
  modalInstance.hide();
  document.activeElement?.blur();
}
      this.loadTasks();
    }
  },

  async deleteTask(id) {
    if (!confirmAction('Delete this task? This cannot be undone.')) return;
    const result = await Api.deleteTask(id);
    if (result) {
      Toast.success('Task deleted.');
      this.loadTasks();
    }
  },
};

function escHtml(s) {
  return String(s || '').replace(/&/g,'&amp;').replace(/</g,'&lt;').replace(/>/g,'&gt;');
}
function exportTasksCSV() {

  if (!allTasks || !allTasks.length) {
    Toast.warning('No tasks to export');
    return;
  }

  var headers = [
    'Title',
    'Status',
    'Priority',
    'Assigned User',
    'Project',
    'Due Date'
  ];

  var rows = allTasks.map(function(t) {

    return [
      '"' + (t.title || '') + '"',
      '"' + (t.status || '') + '"',
      '"' + (t.priority || '') + '"',
      '"' + (t.assignedUser?.name || '') + '"',
      '"' + (t.project?.name || '') + '"',
      '"' + (t.dueDate || '') + '"'
    ].join(',');

  });

  var csv =
    headers.join(',') + '\n' +
    rows.join('\n');

  var blob = new Blob([csv], {
    type: 'text/csv;charset=utf-8;'
  });

  var url = URL.createObjectURL(blob);

  var a = document.createElement('a');

  a.href = url;

  a.download = 'tasks.csv';

  document.body.appendChild(a);

  a.click();

  document.body.removeChild(a);

  URL.revokeObjectURL(url);

  Toast.success('CSV exported successfully');
}
window.exportTasksCSV = exportTasksCSV;
window.TasksPage = TasksPage;