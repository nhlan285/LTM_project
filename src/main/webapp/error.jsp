<%@ page contentType="text/html;charset=UTF-8" language="java"
isErrorPage="true" %>
<!DOCTYPE html>
<html>
  <head>
    <meta charset="UTF-8" />
    <meta name="viewport" content="width=device-width, initial-scale=1.0" />
    <title>Error</title>
    <style>
      * {
        margin: 0;
        padding: 0;
        box-sizing: border-box;
      }

      body {
        font-family: "Segoe UI", Tahoma, Geneva, Verdana, sans-serif;
        background: linear-gradient(135deg, #06b6d4 0%, #0891b2 100%);
        min-height: 100vh;
        display: flex;
        justify-content: center;
        align-items: center;
        padding: 20px;
      }

      .container {
        background: white;
        border-radius: 15px;
        box-shadow: 0 10px 40px rgba(0, 0, 0, 0.2);
        padding: 40px;
        max-width: 500px;
        width: 100%;
        text-align: center;
      }

      .error-icon {
        font-size: 80px;
        margin-bottom: 20px;
      }

      h1 {
        color: #333;
        margin-bottom: 15px;
      }

      p {
        color: #666;
        margin-bottom: 30px;
      }

      .btn {
        background: linear-gradient(135deg, #06b6d4 0%, #0891b2 100%);
        color: white;
        border: none;
        padding: 15px 30px;
        border-radius: 8px;
        font-size: 16px;
        font-weight: bold;
        cursor: pointer;
        text-decoration: none;
        display: inline-block;
        box-shadow: 0 4px 12px rgba(6, 182, 212, 0.3);
      }

      .btn:hover {
        transform: translateY(-2px);
        box-shadow: 0 8px 20px rgba(6, 182, 212, 0.5);
      }

      .error-details {
        background: #f8f9fa;
        border-left: 4px solid #ef4444;
        padding: 15px;
        margin-top: 20px;
        text-align: left;
        border-radius: 8px;
        max-height: 300px;
        overflow-y: auto;
      }

      .error-details pre {
        white-space: pre-wrap;
        word-wrap: break-word;
        font-size: 12px;
        color: #dc2626;
        font-family: "Courier New", monospace;
      }
    </style>
  </head>
  <body>
    <div class="container">
      <div class="error-icon">❌</div>
      <h1>Oops! Something went wrong</h1>
      <p>We're sorry, but an error occurred while processing your request.</p>

      <% if (exception != null) { %>
      <div class="error-details">
        <strong>Error Details:</strong>
        <pre>
<%= exception.getClass().getName() %>: <%= exception.getMessage() != null ? exception.getMessage() : "No message available" %></pre
        >
        <% if (exception.getCause() != null) { %>
        <strong>Caused by:</strong>
        <pre>
<%= exception.getCause().getClass().getName() %>: <%= exception.getCause().getMessage() %></pre
        >
        <% } %>
      </div>
      <% } %>

      <a href="index.jsp" class="btn">🏠 Go Home</a>
    </div>
  </body>
</html>
