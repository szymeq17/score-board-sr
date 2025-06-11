# Assumptions

The method that starts a game returns a `Game` object along with its ID.
Updating the score and finishing the game are performed using the ID.

The summary is returned as a properly sorted list containing `Game` objects with the required information.

Since this was intended to be a simple library rather than a REST API, I did not create Data Transfer Objects.