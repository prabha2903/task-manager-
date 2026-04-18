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
        return null;
      }

      if (res.status === 204) return { success: true };

      const data = await res.json().catch(() => ({}));

      if (!res.ok) {
        console.error("API ERROR:", data);
        return null;
      }

      return data;

    } catch (err) {
      console.error('[API Error]', err);
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

  // -----------------------------------------------------------
  // Tasks
  // -----------------------------------------------------------
  async getTasks(params = {}) {
    return this.get('/tasks/search', params); // pagination + filters
  },

  async getTask(id) {
    return this.get(`/tasks/${id}`);
  },

  async createTask(data) {
    return this.post('/tasks', data);
  },

  async updateTask(id, data) {
    return this.put(`/tasks/${id}`, data);
  },

  async deleteTask(id) {
    return this.delete(`/tasks/${id}`);
  }
};

window.Api = Api;