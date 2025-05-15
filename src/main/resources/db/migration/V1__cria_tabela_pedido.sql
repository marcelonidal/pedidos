-- Cria tabela pedido
CREATE TABLE public.pedido (
    id uuid NOT NULL,
    cliente_id uuid NULL,
    data_criacao timestamp(6) NULL,
    status varchar(255) NULL,
    valor_total numeric(38, 2) NULL,
    CONSTRAINT pedido_pkey PRIMARY KEY (id),
    CONSTRAINT pedido_status_check CHECK (
        status IN ('CRIADO', 'PAGO', 'ENVIADO', 'CANCELADO')
    )
);

-- Cria tabela item_pedido
CREATE TABLE public.item_pedido (
    id uuid NOT NULL,
    preco_unitario numeric(38, 2) NULL,
    produto_id uuid NULL,
    quantidade int4 NOT NULL,
    pedido_id uuid NULL,
    CONSTRAINT item_pedido_pkey PRIMARY KEY (id)
);

-- FK item_pedido → pedido
ALTER TABLE public.item_pedido
ADD CONSTRAINT fk_item_pedido_pedido
FOREIGN KEY (pedido_id) REFERENCES public.pedido(id);