
    create sequence BoardEntity_SEQ start with 1 increment by 50;

    create sequence GameEntity_SEQ start with 1 increment by 50;

    create sequence ScoreEntity_SEQ start with 1 increment by 50;

    create sequence user_table_SEQ start with 1 increment by 50;

    create table BoardEntity (
        id bigint not null,
        cells jsonb,
        columns integer not null,
        name varchar(255),
        rows integer not null,
        primary key (id)
    );

    create table GameEntity (
        id bigint not null,
        blasted jsonb,
        cells jsonb,
        columns integer not null,
        completed timestamp(6),
        rows integer not null,
        score integer not null,
        started timestamp(6),
        board_id bigint,
        user_id bigint,
        primary key (id)
    );

    create table ScoreEntity (
        id bigint not null,
        created timestamp(6),
        score integer not null,
        board_id bigint,
        user_id bigint,
        primary key (id)
    );

    create table user_table (
        id bigint not null,
        authId varchar(255),
        email varchar(255) not null,
        firstName varchar(255),
        isAdmin boolean not null,
        lastName varchar(255),
        tenantId varchar(255),
        userName varchar(255),
        primary key (id),
        constraint UKqr8msbrh4ksy0f8jtgy7itt5l unique (tenantId, authId)
    );

    alter table if exists GameEntity
       add constraint FK1lgdkx891x9svvvgboyvjoaie
       foreign key (board_id)
       references BoardEntity;

    alter table if exists GameEntity
       add constraint FK7vxg81wavjt76bx5b1lrsfjmw
       foreign key (user_id)
       references user_table;

    alter table if exists ScoreEntity
       add constraint FKanq04ctj0y1cmxfahmpqvtkni
       foreign key (board_id)
       references BoardEntity;

    alter table if exists ScoreEntity
       add constraint FK6sf5tvq95otis7sebcm939eco
       foreign key (user_id)
       references user_table;
