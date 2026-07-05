const ReportsPage = {

  async init() {

    this.loadBurndown();

    this.loadTrend();

    this.loadVelocity();
  },

  async loadBurndown() {

    const data = await Api.getBurndownReport() || [];

    new Chart(document.getElementById('burndownChart'), {
      type: 'line',
      data: {
        labels: data.map(x => x.date),
        datasets: [{
          label: 'Remaining Tasks',
          data: data.map(x => x.remaining)
        }]
      }
    });
  },

  async loadTrend() {

    const data = await Api.getTaskTrendReport() || [];

    new Chart(document.getElementById('trendChart'), {
      type: 'bar',
      data: {
        labels: data.map(x => x.date),
        datasets: [{
          label: 'Created Tasks',
          data: data.map(x => x.count)
        }]
      }
    });
  },

  async loadVelocity() {

    const data = await Api.getUserVelocityReport() || [];

    new Chart(document.getElementById('velocityChart'), {
      type: 'bar',
      data: {
        labels: data.map(x => x.user),
        datasets: [{
          label: 'Tasks Completed',
          data: data.map(x => x.completed)
        }]
      }
    });
  }
};

window.ReportsPage = ReportsPage;