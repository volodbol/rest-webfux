CREATE TABLE IF NOT EXISTS project
(
    id         integer auto_increment PRIMARY KEY,
    name       varchar(255),
    updated_at timestamp,
    created_at timestamp
);

CREATE TABLE IF NOT EXISTS task
(
    id          integer auto_increment PRIMARY KEY,
    description varchar(255),
    project_id  varchar(255),
    updated_at  timestamp,
    created_at  timestamp,
    FOREIGN KEY (project_id) REFERENCES project (id)
);