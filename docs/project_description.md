TODO: short package description

### initial dummy data:
``src\main\resources\initial-data```

Manage dummy data repositories: 
pl.psnc.vtl.portal.conf.startup.InitialDataLoader;


### JWT sign in  

POST: ```api/auth/sign-in``` 

BODY: ```{"email": "email2@wp.pl", "password": "myPassword"}```

Headers: ```authorization```


### Security config location
`pl.psnc.vtl.portal.security.WebSecurity`