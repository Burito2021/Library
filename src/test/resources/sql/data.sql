INSERT INTO users
(username, "name", surname, email, phone_number, address, moderation_state, user_state, role_type, "password")
VALUES ('user_3', 'Name_227', 'Surname_285', 'user_3@example.com', '555151342', 'Street 151, City 31, State 39',
        'APPROVED', 'ACTIVE', 'USER', '$2a$10$c8tdyhhRHDCf71vVARQZ3eMu2Pw0b46T.i3MJIwelOduaDXZmlXgi');
INSERT INTO users
(username, "name", surname, email, phone_number, address, moderation_state, user_state, role_type, "password")
VALUES ('user_1', 'Name_940', 'Surname_934', 'user_1@example.com', '555946646', 'Street 688, City 90, State 49',
        'APPROVED', 'ACTIVE', 'ADMIN', '$2a$10$upL945rfcSKrz7mJ5ltPTuJfS1QJ56j4vq3T/e0Jeng1yf/yTChXO');
INSERT INTO users
(username, "name", surname, email, phone_number, address, moderation_state, user_state, role_type, "password")
VALUES ('user_2', 'Name_103', 'Surname_376', 'user_2@example.com', '555493175', 'Street 561, City 82, State 27',
        'APPROVED', 'ACTIVE', 'USER', '$2a$10$c8tdyhhRHDCf71vVARQZ3eMu2Pw0b46T.i3MJIwelOduaDXZmlXgi');

INSERT INTO BOOKS (TITLE, AUTHOR, DESCRIPTION, PUBLISHER, EDITION, PUBLICATION_YEAR)
VALUES ('The Great Gatsby', 'F. Scott Fitzgerald', 'A classic novel set in the Jazz Age', 'Scribner', '1st', 1925),
       ('To Kill a Mockingbird', 'Harper Lee', 'A novel about racial injustice in the Deep South',
        'J.B. Lippincott & Co.', '1st', 1960),
       ('1984', 'George Orwell', 'A dystopian novel about totalitarianism', 'Secker & Warburg', '1st', 1949),
       ('Pride and Prejudice', 'Jane Austen', 'A classic romance novel', 'T. Egerton', '1st', 1813),
       ('The Catcher in the Rye', 'J.D. Salinger', 'A novel about teenage rebellion', 'Little, Brown and Company',
        '1st', 1951),
       ('Moby-Dick', 'Herman Melville', 'A whaling adventure and philosophical exploration', 'Harper & Brothers', '1st',
        1851),
       ('War and Peace', 'Leo Tolstoy', 'A historical novel about the Napoleonic Wars', 'The Russian Messenger', '1st',
        1869),
       ('The Odyssey', 'Homer', 'An epic poem about the adventures of Odysseus', 'Penguin Classics', 'Modern', -800),
       ('The Hobbit', 'J.R.R. Tolkien', 'A fantasy adventure prequel to The Lord of the Rings', 'Allen & Unwin', '1st',
        1937),
       ('The Lord of the Rings: The Fellowship of the Ring', 'J.R.R. Tolkien',
        'The first part of the epic fantasy trilogy', 'Allen & Unwin', '1st', 1954);


INSERT INTO GENRES(NAME)
VALUES ('Action'),
       ('Adventure'),
       ('Comedy'),
       ('Drama'),
       ('Fantasy'),
       ('Horror'),
       ('Mystery'),
       ('Romance'),
       ('Sci-Fi'),
       ('Thriller');