async function fetchTasks() {
  const token = localStorage.getItem("token");
  if (!token) {
    alert("⚠️ Session expired or not logged in. Redirecting to login...");
    window.location.href = "login.html";
    return;
  }

  try {
    const res = await fetch("http://localhost:8080/api/tasks", {
      headers: {
        "Authorization": "Bearer " + token,
        "Content-Type": "application/json",
      },
    });

    if (!res.ok) throw new Error("Unauthorized");

    const tasks = await res.json();
    updateDashboard(tasks);
  } catch (error) {
    console.error("Error fetching tasks:", error);
    alert("⚠️ Session expired or unauthorized. Redirecting to login...");
    localStorage.removeItem("token");
    window.location.href = "login.html";
  }
}

function updateDashboard(tasks) {
  const todayTasks = document.getElementById("todayTasks");
  const upcomingTasks = document.getElementById("upcomingTasks");
  const taskTableBody = document.querySelector("#taskTable tbody");

  todayTasks.innerHTML = "";
  upcomingTasks.innerHTML = "";
  taskTableBody.innerHTML = "";

  const today = new Date().toDateString();

  tasks.forEach((task) => {
    const dueDate = new Date(task.dueDate).toDateString();
    const taskItem = document.createElement("li");
    taskItem.textContent = `${task.summary} - ${dueDate}`;

    dueDate === today
      ? todayTasks.appendChild(taskItem)
      : upcomingTasks.appendChild(taskItem);

    const row = document.createElement("tr");
    row.innerHTML = `
      <td>${task.name}</td>
      <td>${task.description}</td>
      <td>${task.category}</td>
      <td>${dueDate}</td>
      <td>${task.status}</td>
      <td>
        <button onclick="editTask(${task.id})">Edit</button>
        <button onclick="deleteTask(${task.id})">Delete</button>
      </td>
    `;
    taskTableBody.appendChild(row);
  });
}

document.getElementById("taskForm").addEventListener("submit", async function (e) {
  e.preventDefault();

  const token = localStorage.getItem("token");
  if (!token) {
    alert("⚠️ Please login first.");
    window.location.href = "login.html";
    return;
  }

  const taskData = {
    name: document.getElementById("taskName").value,
    summary: document.getElementById("taskSummary").value,
    description: "", // optional field (not in form, set to empty)
    category: document.getElementById("taskCategory").value,
    dueDate: document.getElementById("taskDueDate").value,
    status: document.getElementById("taskStatus").value,
  };

  try {
    const response = await fetch("http://localhost:8080/api/tasks", {
      method: "POST",
      headers: {
        "Content-Type": "application/json",
        "Authorization": "Bearer " + token,
      },
      body: JSON.stringify(taskData),
    });

    if (response.ok) {
      alert("✅ Task created successfully");
      document.getElementById("taskForm").reset();
      fetchTasks();
    } else {
      const msg = await response.text();
      alert("❌ Task creation failed: " + msg);
    }
  } catch (err) {
    console.error("❌ Error creating task:", err);
    alert("Something went wrong!");
  }
});

async function deleteTask(id) {
  const token = localStorage.getItem("token");
  if (!token) {
    alert("Session expired. Please login again.");
    window.location.href = "login.html";
    return;
  }

  try {
    const res = await fetch(`http://localhost:8080/api/tasks/${id}`, {
      method: "DELETE",
      headers: {
        Authorization: `Bearer ${token}`,
      },
    });

    if (res.ok) {
      fetchTasks();
    } else {
      alert("Failed to delete task.");
    }
  } catch (err) {
    console.error("Error deleting task:", err);
  }
}

function download(type) {
  const token = localStorage.getItem("token");
  if (!token) {
    alert("❌ You must be logged in to export tasks.");
    return;
  }

  fetch(`http://localhost:8080/api/export/${type}`, {
    headers: {
      Authorization: `Bearer ${token}`
    }
  })
    .then(response => {
      if (!response.ok) {
        throw new Error("❌ Export failed");
      }
      return response.blob();
    })
    .then(blob => {
      const url = window.URL.createObjectURL(blob);
      const a = document.createElement("a");
      a.href = url;
      a.download = `tasks.${type}`;
      document.body.appendChild(a);
      a.click();
      a.remove();
    })
    .catch(err => {
      console.error(err);
      alert(err.message);
    });
}

function editTask(id) {
  alert("🛠️ Edit functionality coming soon!");
}

function logout() {
  localStorage.removeItem("token");
  window.location.href = "login.html";
}

function sortTasks() {
  alert("🛠️ Sorting functionality coming soon!");
}

// Dummy AI buttons functionality
function predictCategory() {
  alert("🤖 Predict Category feature coming soon!");
}

function generateDescription() {
  alert("🤖 Generate Description feature coming soon!");
}

function generateReport() {
  alert("📋 Admin Report generation coming soon!");
}

window.onload = fetchTasks;
