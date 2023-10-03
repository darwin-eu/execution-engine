# USE ONLY EVEN NUMBERS OF NODE (THESE ARE LTS)
FROM node:20-alpine AS BUILDER

# INSTALL DEPENDENCIES
WORKDIR /deck
COPY tsconfig.json ./
COPY package.json ./
RUN npm install

# BUILD CODE
COPY tsconfig.node.json ./
COPY index.html ./
COPY public ./public
COPY src ./src
RUN npm run build


FROM nginx:1.25-alpine

# COPY CONFIG AND BUILD OUTPUT
RUN rm -rf /usr/share/nginx/html/*
COPY nginx.conf /etc/nginx/conf.d/default.conf
COPY --from=BUILDER /deck/dist /usr/share/nginx/html

EXPOSE 4200

# ENABLE USE OF RUNTIME ENVIRONMENT VARIABLES
WORKDIR /usr/share/nginx/html
COPY ./env.sh .
COPY .env .
RUN apk add --no-cache bash
RUN chmod +x env.sh

CMD ["/bin/bash", "-c", "/usr/share/nginx/html/env.sh && nginx -g \"daemon off;\""]
