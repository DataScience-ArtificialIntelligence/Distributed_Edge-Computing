FROM openjdk:11

# Set working directory
WORKDIR /app

# Copy the project files
COPY ./target/*.jar /app/
COPY ./uploads/ /app/uploads/
COPY ./downloads/ /app/downloads/

# Make directories if they don't exist
RUN mkdir -p /app/uploads /app/downloads

# Expose RMI ports
EXPOSE 1099
EXPOSE 1098

# Command to run the application
# Replace peer-app.jar with your actual JAR name
CMD ["java", "-Djava.rmi.server.hostname=0.0.0.0", "-jar", "/app/peer-app.jar"]