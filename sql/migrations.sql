CREATE TABLE members (
    id INT NOT NULL AUTO_INCREMENT,
    firstName VARCHAR(45),
    lastName VARCHAR(45),
    gender VARCHAR(45),
    PRIMARY KEY(id)
);

CREATE TABLE scans (
    id INT NOT NULL AUTO_INCREMENT,
    scanIn BOOLEAN,
    date DATETIME,
    memberId INT NOT NULL,
    PRIMARY KEY(id),
    FOREIGN KEY(memberId) REFERENCES members(id)
);

CREATE TABLE employees (
    id INT NOT NULL AUTO_INCREMENT,
    firstName VARCHAR(45),
    lastName VARCHAR(45),
    hoursPerWeek INT,
    wage DOUBLE,
    PRIMARY KEY(id)
);  

