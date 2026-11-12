const loginForm = document.getElementById("loginForm");
const message = document.getElementById("message");

loginForm.addEventListener("submit", function(event) {

    event.preventDefault();

    const username =
        document.getElementById("username").value.trim();

    const password =
        document.getElementById("password").value;


    // Check for empty fields

    if (username === "" || password === "") {

        message.textContent =
            "Please enter username and password.";

        message.className =
            "error-message";

        return;
    }


    // Prepare data for AuthServlet

    const formData =
        new URLSearchParams();

    formData.append("action", "login");
    formData.append("username", username);
    formData.append("password", password);


    // Send login request to server

    fetch("auth", {

        method: "POST",

        headers: {
            "Content-Type":
                "application/x-www-form-urlencoded"
        },

        body: formData.toString()

    })

    .then(response => response.json())

    .then(data => {

        if (data.success) {

            message.textContent =
                "Login successful. Welcome, "
                + data.name + "!";

            message.className =
                "success-message";


            /*
             * Temporary storage for displaying
             * the logged-in user's name.
             *
             * Authentication itself is now
             * handled by HttpSession on the server.
             */

            localStorage.setItem(
                "loggedInUser",
                JSON.stringify({
                    username: data.username,
                    name: data.name,
                    role: data.role
                })
            );


            setTimeout(function() {

                window.location.href = "dashboard.jsp";
            }, 800);

        }

        else {

            message.textContent =
                data.message ||
                "Invalid username or password.";

            message.className =
                "error-message";
        }

    })

    .catch(error => {

        console.error(error);

        message.textContent =
            "Unable to connect to the server.";

        message.className =
            "error-message";

    });

});


function togglePassword() {

    const passwordField =
        document.getElementById("password");


    if (passwordField.type === "password") {

        passwordField.type = "text";

    }

    else {

        passwordField.type = "password";

    }
}