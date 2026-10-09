#  ShieldGateway

Proxy reverso e API Gateway desenvolvido em **Java 21** e **Spring Boot 3**, com controle de Rate Limiting distribuído via **Redis** e validação de tokens JWT.

[![Java 21](https://img.shields.io/badge/Java-21-ED8B00?style=flat-square&logo=openjdk&logoColor=white)](https://openjdk.org/)
[![Spring Boot 3.3](https://img.shields.io/badge/Spring_Boot-3.3-6DB33F?style=flat-square&logo=springboot&logoColor=white)](https://spring.io/projects/spring-boot)
[![Redis](https://img.shields.io/badge/Redis-7-DC382D?style=flat-square&logo=redis&logoColor=white)](https://redis.io/)
[![Docker](https://img.shields.io/badge/Docker-Enabled-2496ED?style=flat-square&logo=docker&logoColor=white)](https://www.docker.com/)

---

## Visão Geral

O **ShieldGateway** é um projeto prático para centralizar a camada de entrada de microsserviços. Ele gerencia o fluxo de requisições, valida a autenticação por token JWT, aplica limites de chamadas por IP e faz o roteamento dos pacotes para as APIs destino.

### Status dos Recursos

| Recurso | Status | Detalhes |
| :--- | :---: | :--- |
| **Filtro de Autenticação JWT** | 🟡 Parcial | Intercepta headers `Authorization: Bearer` e valida estrutura do token |
| **Rate Limiter (Token Bucket)** | 🟢 Concluído | Algoritmo integrado ao Redis com controle de expiração (TTL) |
| **Proxy Reverso & Roteamento** | 🟢 Concluído | Encaminha requisições HTTP e trata erros de rede (`502` / `504`) |
| **Docker & Podman** | 🟢 Concluído | Orquestração de containers com `docker-compose` |
| **Métricas & Observabilidade** | 🔴 Pendente | Integração com Actuator e Prometheus |

---

##  Fluxo da Aplicação

```text
  [ Cliente HTTP ]
         │
         ▼ (http://localhost:8080)
┌────────────────────────────────────────────────────────┐
│                     ShieldGateway                      │
│                                                        │
│  1. AuthenticationFilter (Valida Token JWT)            │
│  2. RateLimiterFilter   (Consulta o Redis) ────────────┼──► [ Redis 7 ]
│  3. RouteResolver        (Identifica o destino)        │
│  4. HttpReverseProxy     (Repassa a requisição)        │
└────────────────────────────┬───────────────────────────┘
                             │
                             ▼
                    [ API Interna / Target ]
