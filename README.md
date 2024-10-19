HostServer - Distributed Intelligent Agent Migration in Java
This project is focused on running a HostServer that enables the spawning and migration of distributed intelligent agents using Java. The server interacts with a web-based client, allowing users to create agents and track their movements across different host servers.

Project Overview:
HostServer.java: This file contains the code for running the HostServer, which accepts connections from a browser and spawns new agents. The agents are then capable of migrating between different host servers as per the instructions.
The server provides a web interface for interacting with agents, accessible via a browser (e.g., Firefox) at http://localhost:4242.
The web interface allows users to view the agents' locations and manage their migration in real-time.

Key Features:
Agent Spawning: Users can spawn new agents through the web interface.
Agent Migration: Agents can be migrated between different hosts, demonstrating the distributed nature of the system.
Browser Integration: Interact with the server via a browser at the specified URL to observe and control the behavior of the agents.

Technical Details:
Developed using Java with multi-threaded server-side functionality to handle the spawning and migration of agents.
Web-based client interaction: The server provides an HTML page that can be accessed through a browser for real-time interaction.
The server runs locally and interacts with the browser to demonstrate the migration of distributed intelligent agents across different environments.

How to Run the Project:
Compile the file using javac HostServer.java.
Run the server with java HostServer.
Open a browser (e.g., Firefox) and navigate to http://localhost:4242 to view and interact with the server.
Follow the instructions on the web page to spawn new agents and make them migrate.

Custom Modifications:
I added comments throughout the code to explain the functionality of different sections.
Implemented additional logging to track the movement and status of agents in real-time.

This project helped me explore the concept of distributed intelligent agents and how they can be managed across different servers. It also provided a deeper understanding of how to integrate Java with web-based client interactions.
