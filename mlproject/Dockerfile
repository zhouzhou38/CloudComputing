# Use Miniconda as the base image
FROM continuumio/miniconda3

# Set the working directory in the container
WORKDIR /usr/src/app

# Copy the Conda environment file
COPY environment.yml ./

# Create the Conda environment
RUN conda env create -f environment.yml

# Make RUN commands use the new environment:
SHELL ["conda", "run", "-n", "myenv", "/bin/bash", "-c"]

# The code to run when container is started:
ENTRYPOINT ["conda", "run", "-n", "myenv", "python3", "newre3.py"]

# Copy the rest of your application's source code from your host to your image filesystem.
COPY . .

# Expose port 8888 (if you need it)
EXPOSE 8888/udp

