CREATE TABLE refusjonskrav (
    data jsonb NOT NULL
);

CREATE INDEX status ON refusjonskrav ((data  ->> 'status'));
CREATE INDEX id ON refusjonskrav ((data ->> 'id'));
CREATE INDEX virksomhetsnummer ON refusjonskrav ((data ->> 'virksomhetsnummer'));
CREATE INDEX identitetsnummer ON refusjonskrav ((data  ->> 'identitetsnummer'));


CREATE TABLE kvittering (
    data jsonb NOT NULL
);

CREATE INDEX idx_kvittering_status ON kvittering ((data  ->> 'status'));
CREATE INDEX idx_kvittering_id ON kvittering ((data ->> 'id'));
CREATE INDEX idx_kvittering_virksomhetsnummer ON kvittering ((data ->> 'virksomhetsnummer'));

create table bakgrunnsjobb (
   jobb_id uuid unique not null,
   type VARCHAR(100) not null,
   behandlet timestamp,
   opprettet timestamp not null,

   status VARCHAR(50) not null,
   kjoeretid timestamp not null,

   forsoek int not null default 0,
   maks_forsoek int not null,
   data jsonb
);