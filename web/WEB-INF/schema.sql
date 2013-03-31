SET COLLATION ENGLISH STRENGTH SECONDARY;
create table WINE (
  ID        int(11) NOT NULL PRIMARY KEY,
  WINENAME  varchar(45),
  VINTAGE   varchar(45),
  GRAPES    varchar(45),
  COUNTRY   varchar(45),
  REGION    varchar(45),
  DESCRIPTION varchar(2048),
  PICTURE   varchar(256)
);
create sequence WINE_SEQ start with 20;
create index WINE_IDX_WINENAME on WINE (WINENAME);
insert into WINE values (1,'CHATEAU DE SAINT COSME','2009','Grenache / Syrah','France','Southern Rhone / Gigondas',STRINGDECODE('The aromas of fruit and spice give one a hint of the light drinkability of this lovely wine, which makes an excellent complement to fish dishes.'),'saint_cosme.jpg');
insert into WINE values (2,'LAN RIOJA CRIANZA','2006','Tempranillo','Spain','Rioja',STRINGDECODE('A resurgence of interest in boutique vineyards has opened the door for this excellent foray into the dessert wine market. Light and bouncy, with a hint of black truffle, this wine will not fail to tickle the taste buds.'),'lan_rioja.jpg');
insert into WINE values (3,'MARGERUM SYBARITE','2010','Sauvignon Blanc','USA','California Central Coast',STRINGDECODE('The cache of a fine Cabernet in ones wine cellar can now be replaced with a childishly playful wine bubbling over with tempting tastes of\nblack cherry and licorice. This is a taste sure to transport you back in time.'),'margerum.jpg');
insert into WINE values (4,'OWEN ROE "EX UMBRIS"','2009','Syrah','USA','Washington',STRINGDECODE('A one-two punch of black pepper and jalapeno will send your senses reeling, as the orange essence snaps you back to reality. Don''t miss\nthis award-winning taste sensation.'),'ex_umbris.jpg');
insert into WINE values (5,'REX HILL','2009','Pinot Noir','USA','Oregon',STRINGDECODE('One cannot doubt that this will be the wine served at the Hollywood award shows, because it has undeniable star power. Be the first to catch\nthe debut that everyone will be talking about tomorrow.'),'rex_hill.jpg');
insert into WINE values (6,'VITICCIO CLASSICO RISERVA','2007','Sangiovese Merlot','Italy','Tuscany',STRINGDECODE('Though soft and rounded in texture, the body of this wine is full and rich and oh-so-appealing. This delivery is even more impressive when one takes note of the tender tannins that leave the taste buds wholly satisfied.'),'viticcio.jpg');
insert into WINE values (7,'CHATEAU LE DOYENNE','2005','Merlot','France','Bordeaux',STRINGDECODE('Though dense and chewy, this wine does not overpower with its finely balanced depth and structure. It is a truly luxurious experience for the\nsenses.'),'le_doyenne.jpg');
insert into WINE values (8,'DOMAINE DU BOUSCAT','2009','Merlot','France','Bordeaux',STRINGDECODE('The light golden color of this wine belies the bright flavor it holds. A true summer wine, it begs for a picnic lunch in a sun-soaked vineyard.'),'bouscat.jpg');
insert into WINE values (9,'BLOCK NINE','2009','Pinot Noir','USA','California',STRINGDECODE('With hints of ginger and spice, this wine makes an excellent complement to light appetizer and dessert fare for a holiday gathering.'),'block_nine.jpg');
insert into WINE values (10,'DOMAINE SERENE','2007','Pinot Noir','USA','Oregon',STRINGDECODE('Though subtle in its complexities, this wine is sure to please a wide range of enthusiasts. Notes of pomegranate will delight as the nutty finish completes the picture of a fine sipping experience.'),'domaine_serene.jpg');
insert into WINE values (11,'BODEGA LURTON','2011','Pinot Gris','Argentina','Mendoza',STRINGDECODE('Solid notes of black currant blended with a light citrus make this wine an easy pour for varied palates.'),'bodega_lurton.jpg');
insert into WINE values (12,'LES MORIZOTTES','2009','Chardonnay','France','Burgundy',STRINGDECODE('Breaking the mold of the classics, this offering will surprise and undoubtedly get tongues wagging with the hints of coffee and tobacco in\nperfect alignment with more traditional notes. Breaking the mold of the classics, this offering will surprise and\nundoubtedly get tongues wagging with the hints of coffee and tobacco in\nperfect alignment with more traditional notes. Sure to please the late-night crowd with the slight jolt of adrenaline it brings.'),'morizottes.jpg');
insert into WINE values (13,'ÜRZIGER WÜRZGARTEN','2009','Riesling','Germany','Mosel',STRINGDECODE('This Riesling comes from some fantastic terroir - and besides we need some Umlaut testing here.'),'morizottes.jpg');
