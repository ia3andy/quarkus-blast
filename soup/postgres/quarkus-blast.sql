
    create sequence board_SEQ start with 1 increment by 50;

    create sequence game_SEQ start with 1 increment by 50;

    create sequence player_SEQ start with 1 increment by 50;

    create sequence score_SEQ start with 1 increment by 50;

    create table board (
        id bigint not null,
        cells jsonb,
        columns integer not null,
        name varchar(255),
        rows integer not null,
        primary key (id)
    );

    create table game (
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

    create table player (
        id bigint not null,
        authId varchar(255),
        email varchar(255) not null,
        firstName varchar(255),
        isAdmin boolean not null,
        lastName varchar(255),
        tenantId varchar(255),
        userName varchar(255),
        primary key (id),
        constraint UK4rgillcwnwi6bv70t16039nfm unique (tenantId, authId)
    );

    create table score (
        id bigint not null,
        created timestamp(6),
        score integer not null,
        board_id bigint,
        user_id bigint,
        primary key (id),
        constraint UKo6s1kff5nbxso60akmnxb9mhy unique (board_id, user_id)
    );

    alter table if exists game
       add constraint FK7xfop7fngh26l311rh6nevt09
       foreign key (board_id)
       references board;

    alter table if exists game
       add constraint FKin4h8o8ybi5sfmqsaqg40xut3
       foreign key (user_id)
       references player;

    alter table if exists score
       add constraint FK1ansl6hy9xqpr3e2l95yp1pqr
       foreign key (board_id)
       references board;

    alter table if exists score
       add constraint FKkk3rimvuokqfn3oet1c1i1jt5
       foreign key (user_id)
       references player;
