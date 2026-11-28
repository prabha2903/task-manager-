const API_BASE = '/api';

const Api = {
  // -----------------------------------------------------------
  // Token helpers
  // -----------------------------------------------------------
  getToken() {
    const token = localStorage.getItem('jwt_token');
    return token ? token.trim() : null;
  },

  setToken(token) {
    if (!token) return;
    localStorage.setItem('jwt_token', token);
  },

  removeToken() {
    localStorage.removeItem('jwt_token');
    localStorage.removeItem('current_user');
  },

  getUser() {
    try {
      return JSON.parse(localStorage.getItem('current_user'));
    } catch {
      return null;
    }
  },
  
  setUser(user) {
    localStorage.setItem('current_user', JSON.stringify(user));
  },

  isAuthenticated() {
    return !!this.getToken();
  },

  requireAuth() {
    if (!this.isAuthenticated()) {
      window.location.href = 'index.html';
      return false;
    }
    return true;
  },

  // -----------------------------------------------------------
  // Core request method
  // -----------------------------------------------------------
  async request(method, endpoint, body = null, options = {}) {
    const token = this.getToken();

    console.log("🔐 TOKEN:", token);

    const headers = {
      'Content-Type': 'application/json',
      ...(token && { Authorization: `Bearer ${token}` }),
      ...options.headers,
    };

    const config = {
      method,
      headers,
      ...(body && { body: JSON.stringify(body) }),
    };

    try {
      const res = await fetch(`${API_BASE}${endpoint}`, config);

      console.log(`📡 ${method} ${endpoint} →`, res.status);

      if (res.status === 401) {
        this.removeToken();
        window.location.href = 'index.html';
        return null;
      }

      if (res.status === 403) {
        console.warn("❌ 403 Forbidden → Access denied");
        if (window.Toast) {
    Toast.error("Access denied");
  }
        return null;
      }

      if (res.status === 204) return { success: true };

      const data = await res.json().catch(() => ({}));

      if (!res.ok) {
        console.error("API ERROR:", data);
        if (window.Toast) {
    Toast.error(data.message || "Something went wrong");
  }
        return null;
      }

      return data;

    } catch (err) {
      console.error('[API Error]', err);
       if (window.Toast) {
    Toast.error("Server not reachable");
  }
      return null;
    }
  },

  // -----------------------------------------------------------
  // Shorthand methods
  // -----------------------------------------------------------
  get(endpoint, params = {}) {
    const qs = new URLSearchParams(params).toString();
    const url = qs ? `${endpoint}?${qs}` : endpoint;
    return this.request('GET', url);
  },

  post(endpoint, body) {
    return this.request('POST', endpoint, body);
  },

  put(endpoint, body) {
    return this.request('PUT', endpoint, body);
  },

  patch(endpoint, body) {
    return this.request('PATCH', endpoint, body);
  },

  delete(endpoint) {
    return this.request('DELETE', endpoint);
  },

  // -----------------------------------------------------------
  // Auth
  // -----------------------------------------------------------
  async login(email, password) {
    return this.post('/auth/login', { email, password });
  },

  async register(name, email, password) {
    return this.post('/auth/register', { name, email, password });
  },

  // -----------------------------------------------------------
  // Dashboard
  // -----------------------------------------------------------
  async getDashboardStats() {
    return this.get('/dashboard');
  },

  async getRecentTasks() {
    return this.get('/tasks/recent');
  },

  // -----------------------------------------------------------
  // Users
  // -----------------------------------------------------------
  async getUsers() {
    return this.get('/users');
  },
   async updateUser(id, data) {
  return this.put(`/users/${id}`, data);
},
async changePassword(id, data) {
  return this.put(`/users/${id}/password`, data);
},
async changeUserRole(id, role) {
  return this.put(`/users/${id}/role`, {
    role: role
  });
},

  // -----------------------------------------------------------
// Projects
// -----------------------------------------------------------

async getProjects(params = {}) {
  return this.get('/projects', params);
},

async getProject(id) {
  return this.get(`/projects/${id}`);
},

async createProject(data) {
  return this.post('/projects', data);
},

async updateProject(id, data) {
  return this.put(`/projects/${id}`, data);
},

async deleteProject(id) {
  return this.delete(`/projects/${id}`);
},
// -----------------------------------------------------------
// Reports
// -----------------------------------------------------------

async getBurndownReport() {
  return this.get('/reports/burndown');
},

async getTaskTrendReport() {
  return this.get('/reports/task-trend');
},

async getUserVelocityReport() {
  return this.get('/reports/user-velocity');
},
// -----------------------------------------------------------
// Tasks
// -----------------------------------------------------------

// 🔥 SEARCH + PAGINATION (MAIN LIST)
async searchTasks(params = {}) {
  return this.get('/tasks/search', params);
},

// 🔥 KEEP (optional fallback)
async getTasks(params = {}) {
  return this.searchTasks(params);
},

// 🔥 GET SINGLE TASK
async getTask(id) {
  return this.get(`/tasks/${id}`);
},

// 🔥 CREATE
async createTask(data) {
  return this.post('/tasks', data);
},

// 🔥 FULL UPDATE (VERY IMPORTANT)
async updateTask(id, data) {
   console.log("✏️ Updating Task:", id, data);
  return this.put(`/tasks/${id}`, data);
},
 // 🔥 UPDATE TASK STATUS
async updateTaskStatus(id, status) {
return this.put(`/tasks/${id}/status`, { status });
},
 // 🔥 COMMENTS
// 🔥 COMMENTS (FIXED)
async getComments(taskId) {
  return this.get(`/comments/${taskId}`);
},

async addComment(taskId, content) {
  return this.post(`/comments`, {
    taskId: taskId,
    content: content
  });
},
// 🔥 DELETE
async deleteTask(id) {
  return this.delete(`/tasks/${id}`);
},
async updateComment(commentId, content) {

    return this.put(
        '/comments/' + commentId,
        {
            taskId: 0,
            content: content
        }
    );
},

async deleteComment(commentId) {

    return this.delete(
        '/comments/' + commentId
    );
},
async getActivityLog(taskId) {
  return this.getComments(taskId); // safest fallback
},

  // ─── Teams ──────────────────────────────────────────────────────────────────
  async getTeams()               { return this.get('/teams'); },
  async getTeam(id)              { return this.get('/teams/' + id); },
  async createTeam(data)         { return this.post('/teams', data); },
  async updateTeam(id, data)     { return this.put('/teams/' + id, data); },
  async deleteTeam(id)           { return this.delete('/teams/' + id); },
  async addTeamMember(teamId, userId)    { return this.post('/teams/' + teamId + '/members', { userId }); },
  async removeTeamMember(teamId, userId){ return this.delete('/teams/' + teamId + '/members/' + userId); },
  async changeTeamLead(teamId, userId)  { return this.put('/teams/' + teamId + '/lead', { userId }); },

  // ─── Project ↔ Team assignment ───────────────────────────────────────────
  async assignTeamToProject(projectId, teamId) {
    return this.put('/projects/' + projectId + '/team/' + teamId, {});
  },
  async removeTeamFromProject(projectId) {
    return this.delete('/projects/' + projectId + '/team');
  },

  

  // ─── Phase 4: Sprints ──────────────────────────────────────────────────────
  async getSprints(projectId)       { return this.get('/projects/' + projectId + '/sprints'); },
  async createSprint(projectId, d)  { return this.post('/projects/' + projectId + '/sprints', d); },
  async getSprint(id)               { return this.get('/sprints/' + id); },
  async getSprintSummary(id)        { return this.get('/sprints/' + id + '/summary'); },
  async updateSprint(id, d)         { return this.put('/sprints/' + id, d); },
  async changeSprintStatus(id, s)   { return this.put('/sprints/' + id + '/status', { status: s }); },
  async deleteSprint(id)            { return this.delete('/sprints/' + id); },
  async addTaskToSprint(sId, tId)   { return this.post('/sprints/' + sId + '/tasks/' + tId, {}); },
  async removeTaskFromSprint(sId, tId) { return this.delete('/sprints/' + sId + '/tasks/' + tId); },

  // ─── Phase 4: AI Assignment ────────────────────────────────────────────────
  async aiSuggest(projectId)        { return this.post('/ai/suggest/' + projectId, {}); },
  async aiApprove(taskId, userId)   { return this.post('/ai/approve', { taskId, userId }); },
  async aiStatus()                  { return this.get('/ai/status'); },

  // ─── Phase 4: GitHub Sync ─────────────────────────────────────────────────
  async getCommits(projectId)       { return this.get('/projects/' + projectId + '/commits'); },
  async getPullRequests(projectId)  { return this.get('/projects/' + projectId + '/pulls'); },
  async syncCommits(projectId)      { return this.post('/projects/' + projectId + '/sync/commits', {}); },
  async syncPullRequests(projectId) { return this.post('/projects/' + projectId + '/sync/pulls', {}); },

  // ─── Phase 4: Slack ────────────────────────────────────────────────────────
  async slackStatus()               { return this.get('/slack/status'); },
  async slackTest()                 { return this.post('/slack/test', {}); },

  // ─── Phase 4: Global Search ───────────────────────────────────────────────
  async searchTasks(params)         { return this.get('/search/tasks', params); },

  // ─── Phase 3: Project summary + activity ─────────────────────────────────
  async getProjectSummary(projectId)   { return this.get('/projects/' + projectId + '/summary'); },
  async getProjectActivities(projectId, page=0, size=20) {
    return this.get('/projects/' + projectId + '/activities', { page, size });
  },

  // ─── GitHub repository linking ────────────────────────────────────────────
  async linkGitHub(projectId, repoUrl)  {
    return this.put('/projects/' + projectId + '/github', { repoUrl });
  },
  async removeGitHub(projectId) {
    return this.delete('/projects/' + projectId + '/github');
  },

  // ─── Attachments ──────────────────────────────────────────────────────────
  async getAttachments(taskId)  { return this.get('/tasks/' + taskId + '/attachments'); },
  async uploadAttachment(taskId, formData) {
    const token = localStorage.getItem('jwt_token');
    const res   = await fetch(this.BASE_URL + '/tasks/' + taskId + '/attachments', {
      method:  'POST',
      headers: { 'Authorization': 'Bearer ' + token },
      body:    formData
    });
    return res.ok ? res.json() : null;
  },
  async deleteAttachment(id)    { return this.delete('/attachments/' + id); },
  getAttachmentDownloadUrl(id)  { return this.BASE_URL + '/attachments/' + id + '/download'; },

  async filterUsers(params = {}) {
  // Remove keys with empty string values
  const cleaned = {};
  Object.keys(params).forEach(function(k) {
    if (params[k] !== '' && params[k] != null) cleaned[k] = params[k];
  });
  return this.get('/users/filter', cleaned);
},
};


window.Api = Api;