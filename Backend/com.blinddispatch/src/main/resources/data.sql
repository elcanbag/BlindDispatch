-- Insert sample users
INSERT INTO users (active, email, password, username, verified)
VALUES (TRUE, 'elcanbaa@proton.me', 'plaintextpassword', 'testuser', TRUE);

INSERT INTO users (active, email, password, username, verified)
VALUES (TRUE, 'anotheruser@example.com', 'plaintextpassword', 'anotheruser', TRUE);


INSERT INTO verification_codes (code, created_at, expires_at, used, user_id)
VALUES ('123456', CURRENT_TIMESTAMP, DATEADD('MINUTE', 10, CURRENT_TIMESTAMP), FALSE, 1);



