# Build stage for client
FROM node:22 AS client-build
WORKDIR /usr/src/app/client
COPY client/package*.json ./
RUN npm install
COPY client/ ./
RUN npm run build

# Build stage for server
FROM node:22 AS server-build
WORKDIR /usr/src/app/server
COPY server/package*.json ./
RUN npm install
COPY server/ ./
RUN npm run build

# Production stage
FROM node:22-alpine
WORKDIR /usr/src/app
COPY --from=client-build /usr/src/app/client/build ./client/build
COPY --from=server-build /usr/src/app/server/dist ./server/dist
COPY --from=server-build /usr/src/app/server/package*.json ./server/
WORKDIR /usr/src/app/server
RUN npm install --only=production
EXPOSE 5001
CMD ["node", "dist/server.js"]