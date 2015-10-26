-- Database generated with pgModeler (PostgreSQL Database Modeler).
-- pgModeler  version: 0.7.2
-- PostgreSQL version: 9.2
-- Project Site: pgmodeler.com.br
-- Model Author: ---

SET check_function_bodies = false;
-- ddl-end --

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
ALTER SEQUENCE public.history_id_seq OWNER TO postgres;
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
ALTER TABLE public.history OWNER TO postgres;
-- ddl-end --


