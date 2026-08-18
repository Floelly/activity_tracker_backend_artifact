# Introduction

This codebase represents a codebase for testing coding agents on different tasks and is part of a thesis project at university. The branches like F1, F2, R1, B7 represent different bases on which a task starts and each finished test run was saved in another branch with a unique identifier <ID>-<system>-<timestamp> (e.G. B3-pipeline-2026-08-05-19-02-18). All Tasks are found at folder ``tasks/`` in the root [here](https://github.com/Floelly/activity_tracker_tasks).

This Java/maven/Spring Boot project is a REST API for tracking personal activities and originally connects to a mysql database. Information other than the mysql-testcontainers setup is not presented, but any mysql-db will do, if u override the db settings. 

A traditional Controller-Service-Repository layer is built with the help of Spring Validation, DTOs, mapstruct mapper, Entity classes and JPA and Hibernate for persisting. Lombok is used mainly for Getters, Setters and Constructors.

## Run tests and build artifact

The maven wrapper build tool is included and can be used to run the whole build pipeline.

The stages ``test`` and ``verify`` are especially relevant for the coding agents, as they represent the unit test phase and the integration test phase including coverage checks.

## Author
[Floelly](https://github.com/Floelly)

## Contributing
This is a student project and is not actively accepting contributions or feature requests.

## Project Status
Student project finished 08/17/2026 - not maintained

## License
The MIT License (MIT): (see LICENSE file)