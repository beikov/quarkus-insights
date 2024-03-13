insert into customers (id, first_name, last_name, deleted, tax_category_percents)
values(1, 'Tony', 'Stark', false, array[10, 6, 20, 20, 20]);
insert into customers (id, first_name, last_name, deleted, tax_category_percents)
values(2, 'Steve', 'Rogers', false, array[0, 0, 0, 0, 0]);
insert into customers (id, first_name, last_name, deleted, tax_category_percents)
values(3, 'Peter', 'Parker', false, array[0, 0, 0, 0, 0]);
insert into customers (id, first_name, last_name, deleted, tax_category_percents)
values(4, 'Thor', 'Odinson', false, array[0, 0, 0, 0, 0]);
alter sequence customers_seq restart with 5;

insert into product_categories (id, name, parent_category_id) values(1, 'Drinks', null);
insert into product_categories (id, name, parent_category_id) values(2, 'Soft drinks', 1);
insert into product_categories (id, name, parent_category_id) values(3, 'Alcohol', 1);
insert into product_categories (id, name, parent_category_id) values(4, 'Soup', null);
alter sequence product_categories_seq restart with 5;

insert into products (id, name, product_type, category_id, tags) values(1, 'Tap water', 1, 2, array['drink', 'healthy', 'sugar-free']);
insert into products (id, name, product_type, category_id, tags) values(2, 'Juice', 1, 2, array['drink', 'healthy']);
insert into products (id, name, product_type, category_id, tags) values(3, 'Rum', 2, 3, array['drink', 'alcohol']);
insert into products (id, name, product_type, category_id, tags) values(4, 'Soup', 0, 4, array['food', 'warm', 'healthy']);
alter sequence products_seq restart with 5;

insert into product_variants (id, external_id, product_id, name, details, type_details, price_amount, price_currency)
values (1, gen_random_uuid(), 1, 'Tap water', ('Plain tap water', null)::product_variant_details,
        '{"quantityAmount":500,"quantityUnit":"MILLILITER"}', 0.5, 'EUR');
insert into product_variants (id, external_id, product_id, name, details, type_details, price_amount, price_currency)
values (2, gen_random_uuid(), 2, 'Orange juice', ('Freshly pressed orange jucie', 'Orange')::product_variant_details,
        '{"sugarPercent":8,"quantityAmount":250,"quantityUnit":"MILLILITER"}', 1.5, 'EUR');
insert into product_variants (id, external_id, product_id, name, details, type_details, price_amount, price_currency)
values (3, gen_random_uuid(), 2, 'Apple juice', ('Not from concentrate', 'Yellow')::product_variant_details,
        '{"sugarPercent":10,"quantityAmount":250,"quantityUnit":"MILLILITER"}', 1.2, 'EUR');
insert into product_variants (id, external_id, product_id, name, details, type_details, price_amount, price_currency)
values (4, gen_random_uuid(), 3, 'Carribean rum', ('10 year aged', 'Brown')::product_variant_details,
        '{"alcoholPercent":40,"quantityAmount":40,"quantityUnit":"MILLILITER"}', 7.5, 'EUR');
insert into product_variants (id, external_id, product_id, name, details, type_details, price_amount, price_currency)
values (5, gen_random_uuid(), 4, 'Pumpkin soup', ('Butterscotch', 'Orange')::product_variant_details,
        '{"quantityAmount":400,"quantityUnit":"MILLILITER"}', 5.2, 'EUR');
alter sequence product_variants_seq restart with 6;

insert into subscriptions(id, creation_date, customer_id, subscription_interval, subscription_start)
values (1, current_timestamp, 1, interval '1 days', current_timestamp);
insert into subscriptions(id, creation_date, customer_id, subscription_interval, subscription_start)
values (2, current_timestamp, 2, interval '30 days', current_timestamp);
alter sequence subscriptions_seq restart with 6;

insert into subscription_items(subscription_id, product_variant_id, quantity) values (1, 1, 2);
insert into subscription_items(subscription_id, product_variant_id, quantity) values (1, 4, 1);
insert into subscription_items(subscription_id, product_variant_id, quantity) values (2, 1, 1);
insert into subscription_items(subscription_id, product_variant_id, quantity) values (2, 2, 2);

insert into orders(id, status, creation_date, customer_id, created_from_address)
select t.order_id, 0, (current_date + time '10:00:00' at time zone 'UTC') - interval '1 day' * t.idx, 3, '127.0.0.1'::inet
from generate_series(1, 10) with ordinality t(order_id, idx);

insert into orders(id, status, creation_date, customer_id, created_from_address)
select t.order_id, 0, (current_date + time '10:00:00' at time zone 'UTC') - interval '1 day' * t.idx, 4, '127.0.0.1'::inet
from generate_series(11, 15) with ordinality t(order_id, idx);

insert into order_lines(order_id, customer_id, line_number, product_variant_id, quantity, tax_percent, net_amount, net_currency, total_amount, total_currency)
select t.order_id, 3, p.line_number, p.variant_id, 1, 0, 5, 'EUR', 5, 'EUR'
from generate_series(1, 10) t(order_id)
cross join (values (1, 0), (5, 1)) p(variant_id,line_number);

insert into order_lines(order_id, customer_id, line_number, product_variant_id, quantity, tax_percent, net_amount, net_currency, total_amount, total_currency)
select t.order_id, 4, p.line_number, p.variant_id, 1, 0, 5, 'EUR', 5, 'EUR'
from generate_series(11, 15) t(order_id)
cross join (values (1, 0), (2, 1), (3, 2), (5, 3)) p(variant_id,line_number);

-- old orders

insert into orders(id, status, creation_date, customer_id, created_from_address)
select t.order_id, 0, (current_date + time '10:00:00' at time zone 'UTC') - interval '3 months' * t.idx, 3, '127.0.0.1'::inet
from generate_series(16, 20) with ordinality t(order_id, idx);

insert into order_lines(order_id, customer_id, line_number, product_variant_id, quantity, tax_percent, net_amount, net_currency, total_amount, total_currency)
select t.order_id, 3, p.line_number, p.variant_id, 1, 0, 5, 'EUR', 5, 'EUR'
from generate_series(16, 20) t(order_id)
cross join (values (1, 0), (5, 1)) p(variant_id,line_number);

alter sequence orders_seq restart with 21;