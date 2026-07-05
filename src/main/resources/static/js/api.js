const API_BASE = 'http://localhost:8080/api';

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
}
};


window.Api = Api;