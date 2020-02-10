create table Player(
	id smallint,
	name varchar(32) not null,
	display_name varchar(32) not null,
	frequent boolean not null,
	regular boolean not null,
	license varchar(10),
	constraint player_pk primary key(id),
	constraint player_dn unique(display_name)
);

create table RCR_Tournament(
	id smallint,
	name varchar(32) not null,
	constraint rcr_tournament_pk primary key(id),
	constraint rcr_tournament_name unique(name)
);

create table RCR_Game_Id(
	id bigint,
	rcr_tournament_id smallint not null,
	date date not null,
	nb_players smallint not null,
	nb_rounds smallint not null,
	constraint rgi_pk primary key(id),
	constraint rgi_tid_fk foreign key(rcr_tournament_id) references RCR_Tournament(id) on delete restrict on update restrict,
	constraint rgi_nb_players check (nb_players=4 or nb_players=5),
	constraint rgi_nb_rounds check (nb_rounds=1 or nb_rounds=2 or nb_rounds=4)
);

create table RCR_Game_Score(
	rcr_game_id bigint,
	player_id smallint,
	ranking smallint not null,
	game_score integer not null,
	uma_score integer not null,
	final_score integer not null,
	constraint rgs_pk primary key(rcr_game_id, player_id),
	constraint rgs_id_fk foreign key(rcr_game_id) references RCR_Game_Id(id) on delete cascade on update restrict,
	constraint rgs_player_id_fk foreign key(player_id) references Player(id) on delete restrict on update restrict,
	constraint rgs_ranking_intergrity check(ranking>=1 and ranking<=5)
);
