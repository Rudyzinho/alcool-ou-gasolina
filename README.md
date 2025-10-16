# AT02 - Álcool ou Gasolina? - Simples

Aplicativo Android desenvolvido em **Kotlin** com **Jetpack Compose**, que ajuda o usuário a decidir se vale mais a pena abastecer com álcool ou gasolina em seu carro, considerando uma regra percentual simples.

---

##  Objetivo

Facilitar a escolha do combustível mais econômico, permitindo que o usuário:

- Insira preços do álcool e da gasolina.
- Escolha o critério de decisão: álcool deve custar 70% ou 75% do valor da gasolina.
- Salve e gerencie múltiplos postos de combustível.
- Visualize a decisão recomendada para cada posto.

---

##  Funcionalidades Implementadas

1. **Cálculo automático do combustível mais econômico**
   - Percentual definido pelo usuário: **70% ou 75%**.
   - Resultado exibido na tela: `"USE ÁLCOOL"` ou `"USE GASOLINA"`.

2. **Persistência de dados**
   - Lista de postos e critérios de decisão salvos localmente com **DataStore**.
   - Serialização dos dados em JSON usando **Gson**.

3. **Interface interativa**
   - Tela única com campos:
     - Nome do posto (opcional)
     - Preço do álcool
     - Preço da gasolina
     - Critério percentual com **Switch**
   - Lista de postos exibida em **cards**, mostrando preços, razão e recomendação.
   - Botões de **editar** e **remover** cada posto.

4. **Tema e layout customizados**
   - Cores distintas para temas claro e escuro.
   - Layout com **Material Design**, cards com elevação e bordas arredondadas.
   - Ícone próprio do app.

---

##  Estrutura do Código

### 1. **Data Classes**
```kotlin
data class Station(
    val id: Long,
    val name: String,
    val alcoholPrice: Double,
    val gasPrice: Double
)

