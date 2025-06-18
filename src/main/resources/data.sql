-- user data for users
INSERT INTO users (username, password, email, roles, created_at, updated_at)
VALUES
('testuser', '{bcrypt}$2a$10$wwos1JIe9M3XQVkaSB6pie7Q7VkFRO6G3PnEhrsrIYyMXu/AHjkcG', 'testuser@example.com', 'ROLE_USER', '2025-06-11 10:29:20', '2025-06-12 00:26:16');

-- post data for posts
INSERT INTO posts (created_at, updated_at, author, content, title)
VALUES
('2025-06-12 09:27:05.799705', '2025-06-17 10:08:21.356072', 'testuser', '내용(수정)', '제목(수정)');

-- comment data for comments
INSERT INTO comments (created_at, updated_at, content, creator, post_id)
VALUES
('2025-06-17 15:40:28.123338', '2025-06-17 15:40:28.123338', '11111', 'testuser', 1),
('2025-06-17 15:44:29.505606', '2025-06-17 15:44:29.505606', '2222', 'testuser', 1);