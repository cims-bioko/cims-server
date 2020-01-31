-- remove separate privilege for campaign upload, now part of creation/editing
delete from privilege where privilege = 'UPLOAD_CAMPAIGNS';