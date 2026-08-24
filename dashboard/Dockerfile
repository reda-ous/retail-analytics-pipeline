# Build stage: compile the Vue app to static files.
FROM node:22-alpine AS build
WORKDIR /app
COPY package.json package-lock.json ./
RUN npm ci
COPY . .
RUN npm run build

# Runtime stage: nginx just serves the static build and proxies /api to the
# consumer service by its compose service name, the same idea as Vite's own
# dev-server proxy — the browser never talks cross-origin to the consumer.
FROM nginx:alpine
COPY --from=build /app/dist /usr/share/nginx/html
COPY nginx.conf /etc/nginx/conf.d/default.conf
