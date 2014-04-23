INSERT INTO `user` (uuid, firstName, lastName, fullName, description, username, password, lastLoginTime, deleted) VALUES ('e67f2870c59c11e39c1a0800200c9a66', 'Ben', 'Heasly', 'Ben Heasly Import Script', 'Bulk import of sample data from Baney district spreadsheet', 'bsh-bulk-import', 'bsh-bulk-import', unix_timestamp(), false);

INSERT INTO `user_roles` (user_uuid, role_uuid) VALUES ('e67f2870c59c11e39c1a0800200c9a66', 'ROLE1');

INSERT INTO `fieldworker` (uuid, extid, firstname, lastname, deleted) VALUES ('4786a2b0c59d11e39c1a0800200c9a66','FWIMPORT', 'Import', 'Automatic', false);

INSERT INTO `locationhierarchylevel` (uuid,keyIdentifier,name) VALUES('e8031610c5ac11e39c1a0800200c9a66',1,'Region');
INSERT INTO `locationhierarchylevel` (uuid,keyIdentifier,name) VALUES('e8033d20c5ac11e39c1a0800200c9a66',2,'Province');
INSERT INTO `locationhierarchylevel` (uuid,keyIdentifier,name) VALUES('e8033d21c5ac11e39c1a0800200c9a66',3,'District');
INSERT INTO `locationhierarchylevel` (uuid,keyIdentifier,name) VALUES('e8033d22c5ac11e39c1a0800200c9a66',4,'MapArea');
INSERT INTO `locationhierarchylevel` (uuid,keyIdentifier,name) VALUES('e8033d23c5ac11e39c1a0800200c9a66',5,'Sector');

INSERT INTO `locationhierarchy` (uuid,name,extId,level_uuid,parent_uuid,insertDate,collectedBy_uuid,insertBy_uuid,status) VALUES ('36ec15f0c5ae11e39c1a0800200c9a66','Bioko Island','BI','e8031610c5ac11e39c1a0800200c9a66','HIERARCHY_ROOT',date(now()),'4786a2b0c59d11e39c1a0800200c9a66','e67f2870c59c11e39c1a0800200c9a66','A');

