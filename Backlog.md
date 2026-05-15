# !!! 1. Security (!!!Basic, JWT token(ldap, cognito))(in security two endpoints for creating users, updating and retrieving(policies length, one upperCase) )

# 2. Library analytics dashboard (most borrowed items(books))

# 3. General statistics on the in db, number of items, number of users, available items, borrowed items

# 4. Search by title, author, genre, combination of these, exact search, and partial search

# 5. Book reviews and ratings (separate controller and logics)

# 6. Overdue book management (tracking overdue books, automatic fine calculations)-notification of users and administrator

# !!! 7. Role model (admin, user) admin anything, user just give and take books

# 8. Migration from Hibernate to Jooq

# 9. Integrations

# 10. CREATE UI (DESCRIPTION to generate code via cursor)(javascript typescript react, angular )

# 11. Security ( JWT token(ldap, cognito))

# 12. Security ( two factor authentication)

# 13. Activity log ( transactions (login, update - all the actions  actor, time))

# 14. User Summary (bookItems and other info) accessible by admin and by user on user

1. implement toDo note
2. move tokes from db to redis and use time to live
3. Raise Java version to 25

JWT

1. token manager (create, delete revoke)(separate module)
2. validate

3. Switch to springboot 4.0.

Embedded solution (how they offer to implement it)
main point whet they offer
https://medium.com/@victoronu/implementing-refresh-token-logout-in-a-spring-boot-jwt-application-b9d31de953d6

1.

access
refresh token

2. revoke the token (when expiration, when u logged out, u cannot use previous token, fingerprint- simelteous login)
3. prolong the life of token (access and refresh token configuration)
   Implement roles
4. Use springboot 4.0. tools(jwt, signature, clam, token decoding)
5. for jwt springboot should module not manual issuing, logging,authentication
6. (mirconaft, security jwt -= where to store and how to revoke, expose controller)
7. Test coverage (three endpoint sign in, sign out, revoke, refresh toke controllers)

8. Spring doc on errros on filter (find on centralized approach to handling errors)


Homework
1. Dependabot implement in a different brench
2. Bot to automatically rebase/merge changes after approval
3. gradlewbot
4. Policies for naming branches (feature/LP-1, fix/LP-1,version/LP-1 (LP-1- task name))