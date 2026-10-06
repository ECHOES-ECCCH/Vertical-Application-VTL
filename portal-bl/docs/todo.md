## TODO:
 ```SELECT  DISTINCT i FROM Index i join i.verses v join v.page p join p.transcription t WHERE (i.name LIKE :regex OR i.description LIKE :regex) and t.id = :transcriptionId ```
 create index on fields ```name``` and ```description``` inside table ```index```
 
