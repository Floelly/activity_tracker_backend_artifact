CREATE TABLE activity
(
    id          BIGINT       NOT NULL AUTO_INCREMENT,
    business_id CHAR(13)     NOT NULL,
    title       VARCHAR(120) NULL,
    notes       VARCHAR(255) NULL,
    start_at    TIMESTAMP    NOT NULL,
    end_at      TIMESTAMP    NOT NULL,
    created_at  TIMESTAMP    NOT NULL,
    updated_at  TIMESTAMP    NULL,
    PRIMARY KEY (id),
    CONSTRAINT uk_activity_business_id UNIQUE (business_id)
);

CREATE TABLE category
(
    id          BIGINT   NOT NULL AUTO_INCREMENT,
    business_id CHAR(13) NOT NULL,
    name        VARCHAR(50),
    color_code  VARCHAR(9),
    icon_name   VARCHAR(30),
    description VARCHAR(255),
    PRIMARY KEY (id),
    CONSTRAINT uk_category_business_id UNIQUE (business_id)
);

CREATE TABLE tag
(
    id          BIGINT   NOT NULL AUTO_INCREMENT,
    business_id CHAR(13) NOT NULL,
    label       VARCHAR(30),
    color_code  VARCHAR(9),
    description VARCHAR(255),
    sort_order  INT      NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT uk_tag_business_id UNIQUE (business_id)
);

CREATE TABLE sub_category
(
    id          BIGINT   NOT NULL AUTO_INCREMENT,
    business_id CHAR(13) NOT NULL,
    name        VARCHAR(50),
    description VARCHAR(255),
    category_id BIGINT   NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT uk_sub_category_business_id UNIQUE (business_id),
    CONSTRAINT fk_sub_category_category FOREIGN KEY (category_id) REFERENCES category (id)
);

CREATE TABLE activity_attribute
(
    id               BIGINT      NOT NULL AUTO_INCREMENT,
    activity_id      BIGINT      NOT NULL,
    label            VARCHAR(50) NOT NULL,
    value            VARCHAR(255),
    show_in_overview BOOLEAN     NOT NULL DEFAULT FALSE,
    sort_order       INT         NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_activity_attribute_activity FOREIGN KEY (activity_id) REFERENCES activity (id),
    CONSTRAINT uk_activity_attribute_activity_key UNIQUE (activity_id, label)
);

CREATE TABLE category_allocation
(
    id              BIGINT NOT NULL AUTO_INCREMENT,
    activity_id     BIGINT NOT NULL,
    category_id     BIGINT NOT NULL,
    sub_category_id BIGINT NULL,
    percentage      INT    NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_category_allocation_activity FOREIGN KEY (activity_id) REFERENCES activity (id),
    CONSTRAINT fk_category_allocation_category FOREIGN KEY (category_id) REFERENCES category (id),
    CONSTRAINT fk_category_allocation_sub_category FOREIGN KEY (sub_category_id) REFERENCES sub_category (id),
    CONSTRAINT chk_category_allocation_percentage CHECK (percentage BETWEEN 1 AND 100)
);

CREATE TABLE activity_tag
(
    activity_id BIGINT NOT NULL,
    tag_id      BIGINT NOT NULL,
    PRIMARY KEY (activity_id, tag_id),
    CONSTRAINT fk_activity_tag_activity FOREIGN KEY (activity_id) REFERENCES activity (id),
    CONSTRAINT fk_activity_tag_tag FOREIGN KEY (tag_id) REFERENCES tag (id)
);

CREATE INDEX idx_sub_category_category_id ON sub_category (category_id);
CREATE INDEX idx_activity_attribute_activity_id ON activity_attribute (activity_id);
CREATE INDEX idx_category_allocation_activity_id ON category_allocation (activity_id);
CREATE INDEX idx_category_allocation_category_id ON category_allocation (category_id);
CREATE INDEX idx_category_allocation_sub_category_id ON category_allocation (sub_category_id);
CREATE INDEX idx_activity_tag_tag_id ON activity_tag (tag_id);