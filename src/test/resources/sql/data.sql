INSERT INTO USERS (USERNAME, NAME, SURNAME, EMAIL, PHONE_NUMBER, ADDRESS)
VALUES ('user1', 'John', 'Doe', 'john.doe1@example.com', '1234567890', '123 Main St, City, Country'),
       ('user2', 'Jane', 'Smith', 'jane.smith2@example.com', '0987654321', '456 Oak St, City, Country'),
       ('user3', 'Robert', 'Brown', 'robert.brown3@example.com', '5432167890', '789 Pine St, City, Country'),
       ('user4', 'Emily', 'Davis', 'emily.davis4@example.com', '6789054321', '321 Maple St, City, Country'),
       ('user5', 'Michael', 'Johnson', 'michael.johnson5@example.com', '5678901234', '654 Birch St, City, Country');

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