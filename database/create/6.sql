-- Database generated with pgModeler (PostgreSQL Database Modeler).
-- pgModeler  version: 0.7.2
-- PostgreSQL version: 9.2
-- Project Site: pgmodeler.com.br
-- Model Author: ---

SET check_function_bodies = false;
-- ddl-end --


-- Database creation must be done outside an multicommand file.
-- These commands were put in this file only for convenience.
-- -- object: wildfly | type: DATABASE --
-- -- DROP DATABASE wildfly;
-- CREATE DATABASE wildfly
-- 	ENCODING = 'UTF8'
-- 	LC_COLLATE = 'C'
-- 	LC_CTYPE = 'C'
-- 	TABLESPACE = pg_default
-- 	OWNER = postgres
-- ;
-- -- ddl-end --
-- 

-- object: public.history_id_seq | type: SEQUENCE --
-- DROP SEQUENCE public.history_id_seq;
CREATE SEQUENCE public.history_id_seq
	INCREMENT BY 1
	MINVALUE 1
	MAXVALUE 9223372036854775807
	START WITH 1
	CACHE 1
	NO CYCLE
	OWNED BY NONE;
-- ddl-end --

-- object: public.history | type: TABLE --
-- DROP TABLE public.history;
CREATE UNLOGGED TABLE public.history(
	id bigint NOT NULL DEFAULT nextval('history_id_seq'::regclass),
	jid character varying(255),
	status character varying(255),
	timestamp timestamp,
	CONSTRAINT history_pkey PRIMARY KEY (id)

);
-- ddl-end --

-- object: public.version | type: TABLE --
-- DROP TABLE public.version;
CREATE TABLE public.version(
	id bigint NOT NULL
);
-- ddl-end --

delete from
    version
;

insert into
    version(
        id
    )
values
    (6)
;
