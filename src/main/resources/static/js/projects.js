// ============================================================
// projects.js — Project CRUD page logic
// ============================================================

const ProjectsPage = {
  page: 0,
  size: 10,
  totalPages: 0,
  totalElements: 0,
  filters: {
    name: ''
  },
  currentEditId: null,

  async init() {
  const user = Api.getUser();
  const isAdmin = user?.role === 'ADMIN';

  // Hide "New Project" button if NOT admin
  if (!isAdmin) {
    const btn = document.getElementById('new-project-btn');
    if (btn) btn.style.display = 'none';
  }

  await this.loadProjects();
  this.bindEvents();
},

  bindEvents() {
    // Search
const searchInput = document.getElementById('filter-name');
    if (searchInput) {
      searchInput.addEventListener(
        'input',
        debounce(() => {
          this.filters.name = searchInput.value.trim();
          this.page = 0;
          this.loadProjects();
        }, 400)
      );
    }

    // New project button
    document.getElementById('new-project-btn')?.addEventListener('click', () => {
      this.openCreateModal();
    });

    // Save project
    document.getElementById('save-project-btn')?.addEventListener('click', () => {
      this.saveProject();
    });
  },

  buildQueryParams() {
    const params = {
      page: this.page,
      size: this.size
    };

    if (this.filters.name) {
      params.name = this.filters.name;
    }

    return params;
  },

  async loadProjects() {
    const tbody = document.getElementById('projects-tbody');
    if (!tbody) return;

    tbody.innerHTML = `
      <tr>
        <td colspan="5" style="text-align:center;padding:40px">
          <div class="loading-spinner" style="justify-content:center">
            <div class="spinner"></div> Loading projects...
          </div>
        </td>
      </tr>
    `;

    const data = await Api.getProjects(this.buildQueryParams());

    if (!data) {
      tbody.innerHTML = `
        <tr>
          <td colspan="5" style="text-align:center">❌ Failed to load projects</td>
        </tr>
      `;
      return;
    }

    const projects = data.content || (Array.isArray(data) ? data : []);

    this.totalPages = data.totalPages || 1;
    this.totalElements = data.totalElements || projects.length;

    if (!projects.length) {
      tbody.innerHTML = `
        <tr>
          <td colspan="5" style="text-align:center;padding:30px">
            📭 No projects found
          </td>
        </tr>
      `;
    } else {
      tbody.innerHTML = projects.map(p => this.renderRow(p)).join('');
    }

    this.renderPagination();
  },

  renderRow(project) {
  const user = Api.getUser();
 const isAdmin = user?.role === 'ADMIN';

  return `
    <tr>
      <td>${escHtml(project.name)}</td>
      <td>${escHtml(project.description || '—')}</td>
      <td>${formatDate(project.createdAt)}</td>
      <td>
        <div style="display:flex;gap:6px">
        <button class="btn btn-primary btn-sm"
  onclick="window.location.href='project-detail.html?id=${project.id}'">
  👁
</button>
          <button class="btn btn-secondary btn-sm"
            onclick="ProjectsPage.openEditModal(${project.id})">
            ✏️
          </button>

          ${isAdmin ? `
            <button class="btn btn-danger btn-sm"
              onclick="ProjectsPage.deleteProject(${project.id})">
              🗑
            </button>
          ` : ''}
        </div>
      </td>
    </tr>
  `;
},

  renderPagination() {
    const wrap = document.getElementById('pagination-wrap');
    if (!wrap) return;

    wrap.innerHTML = `
      <div class="pagination-controls">
        <button class="page-btn"
          onclick="ProjectsPage.goToPage(${this.page - 1})"
          ${this.page === 0 ? 'disabled' : ''}>
          Prev
        </button>

        <span style="padding:8px 12px">
          Page ${this.page + 1} / ${this.totalPages}
        </span>

        <button class="page-btn"
          onclick="ProjectsPage.goToPage(${this.page + 1})"
          ${this.page >= this.totalPages - 1 ? 'disabled' : ''}>
          Next
        </button>
      </div>
    `;
  },

  goToPage(page) {
    if (page < 0 || page >= this.totalPages) return;
    this.page = page;
    this.loadProjects();
  },

  openCreateModal() {
    this.currentEditId = null;

    document.getElementById('modal-title').textContent = '+ New Project';
    document.getElementById('form-id').value = '';
    document.getElementById('form-name').value = '';
    document.getElementById('form-description').value = '';

    new bootstrap.Modal(document.getElementById('project-modal')).show();
  },

 async openEditModal(id) {
  this.currentEditId = id;

  const projectData = await Api.getProject(id);
  if (!projectData) return;

  // ✅ handle nested response
  const p = projectData.project || projectData;

  document.getElementById('modal-title').textContent = 'Edit Project';
  document.getElementById('form-id').value = p.id;
  document.getElementById('form-name').value = p.name || '';
  document.getElementById('form-description').value = p.description || '';

  new bootstrap.Modal(document.getElementById('project-modal')).show();
},

  async saveProject() {
    const id = document.getElementById('form-id').value;

    const payload = {
      name: document.getElementById('form-name').value.trim(),
      description: document.getElementById('form-description').value.trim()
    };

    if (!payload.name) {
      Toast.error('Project name required');
      return;
    }

    const btn = document.getElementById('save-project-btn');
    btn.disabled = true;
    btn.textContent = 'Saving...';

    let result;

    if (id) {
      result = await Api.updateProject(id, payload);
    } else {
      result = await Api.createProject(payload);
    }

    btn.disabled = false;
    btn.textContent = 'Save Project';

    if (result) {
      Toast.success(id ? 'Project updated!' : 'Project created!');
      bootstrap.Modal.getInstance(
        document.getElementById('project-modal')
      )?.hide();

      this.loadProjects();
    }
  },

  async deleteProject(id) {
    if (!confirmAction('Delete this project?')) return;

    const result = await Api.deleteProject(id);

    if (result) {
      Toast.success('Project deleted');
      this.loadProjects();
    }
  }
};

function escHtml(str) {
  return String(str || '')
    .replace(/&/g, '&amp;')
    .replace(/</g, '&lt;')
    .replace(/>/g, '&gt;');
}

window.ProjectsPage = ProjectsPage;