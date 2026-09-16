# 校园市集前端接口契约

> 依据后端源码整理（`market/controller`、`market/domain`、`common/Result`、`common/ResultCode`、`MarketOrderServiceImpl`）。
> 生成日期：2026-09-15
>
> **修订记录**
> - 2026-09-15 初版（基于 MarketItem / MarketOrder 实体）
> - 2026-09-15 v2：接口 1/2 响应改为 `MarketItemVO`（含 seller 简要信息），接口 8 响应改为 `MarketOrderVO`（含 itemTitle / itemCoverUrl / counterparty），前端 `types/market.ts` 已同步

## 一、通用约定

### 统一响应体 `Result<T>`

所有接口均返回该结构：

| 字段 | 类型 | 说明 |
|------|------|------|
| code | Integer | 状态码，`200` 表示成功 |
| msg | String | 提示信息，成功时为 `"success"` |
| data | T（泛型） | 业务数据，随接口不同而不同 |

**状态码枚举（ResultCode）：**

| code | msg | 含义 |
|------|-----|------|
| 200 | success | 成功 |
| 400 | 参数错误 | 请求参数校验失败（@Valid 触发） |
| 401 | 未认证 | 未登录 / token 失效 |
| 403 | 无权限 | 无权操作（如非卖家本人下架商品） |
| 404 | 资源未找到 | 商品/订单不存在 |
| 500 | 服务器异常 | 服务端错误 |

### 分页结构 `Page<T>`（MyBatis-Plus）

分页接口的 `data` 为该结构，前端主要消费：

| 字段 | 类型 | 说明 |
|------|------|------|
| records | T[] | 当前页数据列表 |
| total | Long（JSON number） | 总记录数 |
| size | Long | 每页条数 |
| current | Long | 当前页码 |
| pages | Long | 总页数 |

### 类型说明

- `Long` / `Integer` → JSON number（⚠️ 超出 JS 安全整数范围有精度风险，自增 ID 一般无碍）
- `BigDecimal` → JSON number（金额建议前端展示时格式化为两位小数）
- `LocalDateTime` → JSON string（ISO-8601 格式，如 `"2026-09-15T10:30:00"`，具体以项目 Jackson 配置为准）
- 字段命名统一为 **camelCase**

---

## 二、商品接口（MarketItem）

### 商品视图对象字段（接口 1/2 响应中 `MarketItemVO` 的结构）

> ⚠️ 与数据库实体 `MarketItem` 不同：VO **不返回** `sellerId` / `updateTime`，卖家信息以 `seller` 对象形式返回。

| 字段 | 类型 | 说明 |
|------|------|------|
| id | Long | 商品 ID |
| title | String | 商品标题 |
| description | String \| null | 商品详细描述 |
| price | BigDecimal | 价格 |
| coverUrl | String | 封面图 URL（DB 默认 `''`） |
| imageUrls | String \| null | 详情图 URL 列表，**JSON 数组格式的字符串**（前端需自行 `JSON.parse`） |
| contactInfo | String | 卖家联系方式 |
| status | Integer | 商品状态枚举，见下 |
| seller | UserSimpleVO | 卖家信息：`{ userId: Long, nickname: String, avatar: String }` |
| createTime | String(DateTime) | 创建时间 |

**商品状态枚举 `status`：**

| 值 | 含义 |
|----|------|
| 0 | 待售 |
| 1 | 锁定中（已下单未支付） |
| 2 | 已售出 |
| 3 | 已下架 |

---

### 1. 获取商品列表

- **Method / Path**：`GET /api/v1/market/items`
- **请求参数（Query）：**

| 参数 | 类型 | 必填 | 默认 | 说明 |
|------|------|------|------|------|
| pageNum | Integer | 否 | 1 | 页码 |
| pageSize | Integer | 否 | 10 | 每页条数 |
| keyword | String | 否 | — | 搜索关键词 |

- **响应**：`Result<Page<MarketItemVO>>`，即 `data.records` 为商品数组，`data.total` 为总数。

### 2. 获取商品详情

- **Method / Path**：`GET /api/v1/market/items/{id}`
- **请求参数（Path）：**

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| id | Long | 是 | 商品 ID |

- **响应**：`Result<MarketItemVO>`，`data` 为单个商品对象（字段同上表）。

### 3. 发布商品

- **Method / Path**：`POST /api/v1/market/items`
- **请求体（JSON，MarketItemPublishDTO）：**

| 字段 | 类型 | 必填 | 校验规则 |
|------|------|------|----------|
| title | String | 是 | 非空；长度 ≤ 100 |
| description | String | 否 | — |
| price | BigDecimal(number) | 是 | 非空；必须 > 0；最多 8 位整数 + 2 位小数 |
| coverUrl | String | 否 | 封面图 URL |
| imageUrls | String | 否 | 详情图列表，JSON 数组格式的字符串 |
| contactInfo | String | 是 | 非空；长度 ≤ 100（微信/电话） |

- **响应**：`Result<Boolean>`，`data` 为 `true`/`false`。
- **备注**：卖家 ID 取自当前登录用户，前端无需传。校验失败返回 code 400，msg 为对应校验消息（如"商品标题不能为空"）。

### 4. 下架商品

- **Method / Path**：`PUT /api/v1/market/items/{id}/off-shelf`
- **请求参数（Path）：**

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| id | Long | 是 | 商品 ID |

- **响应**：`Result<Boolean>`。
- **备注**：仅卖家本人可下架（服务端校验 `sellerId == 当前用户`，否则拒绝）。

---

## 三、订单接口（MarketOrder）

### 订单视图对象字段（接口 8 响应中 `MarketOrderVO` 的结构）

> ⚠️ 与数据库实体 `MarketOrder` 不同：VO **不返回** `buyerId` / `sellerId` / `updateTime`，改为返回商品快照（`itemTitle` / `itemCoverUrl`）与交易对方（`counterparty`）。

| 字段 | 类型 | 说明 |
|------|------|------|
| id | Long | 订单 ID |
| orderSn | String | 唯一订单号（业务操作均以此为准，非 id） |
| itemId | Long | 商品 ID |
| itemTitle | String | 商品标题（商品被删除时返回「商品已删除」） |
| itemCoverUrl | String | 商品封面图 URL |
| amount | BigDecimal | 交易金额 |
| status | Integer | 订单状态枚举，见下 |
| counterparty | UserSimpleVO | 交易对方（买家视角=卖家，卖家视角=买家）：`{ userId, nickname, avatar }` |
| payTime | String(DateTime) \| null | 支付时间（未支付为 null） |
| createTime | String(DateTime) | 创建时间 |

**订单状态枚举 `status`：**

| 值 | 含义 |
|----|------|
| 0 | 待支付 |
| 1 | 已完成（已支付） |
| 2 | 已取消（超时/手动） |

---

### 5. 创建订单（购买）

- **Method / Path**：`POST /api/v1/market/orders`
- **请求体（JSON，MarketOrderCreateDTO）：**

| 字段 | 类型 | 必填 | 校验规则 |
|------|------|------|----------|
| itemId | Long(number) | 是 | 非空；必须 > 0 |

- **响应**：`Result<String>`，`data` 为生成的**订单号 orderSn**（前端后续支付/取消均用此值）。
- **备注**：买家不能购买自己的商品（服务端校验）；下单后商品进入"锁定中"(status=1)；订单有支付超时机制（MQ 延迟消息自动取消）。并发下可能因乐观锁失败（商品已被他人锁定/售出）返回错误。

### 6. 模拟支付

- **Method / Path**：`POST /api/v1/market/orders/{orderSn}/pay`
- **请求参数（Path）：**

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| orderSn | String | 是 | 订单号 |

- **响应**：`Result<Boolean>`。
- **备注**：仅买家本人可支付；支付成功后订单 status→1，商品 status→2（已售出）。

### 7. 取消订单

- **Method / Path**：`POST /api/v1/market/orders/{orderSn}/cancel`
- **请求参数（Path）：**

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| orderSn | String | 是 | 订单号 |

- **响应**：`Result<Boolean>`。
- **备注**：仅买家本人可取消；取消后订单 status→2，商品解锁回到"待售"(status=0)。

### 8. 我的买卖记录

- **Method / Path**：`GET /api/v1/market/orders/my`
- **请求参数（Query）：**

| 参数 | 类型 | 必填 | 默认 | 说明 |
|------|------|------|------|------|
| pageNum | Integer | 否 | 1 | 页码 |
| pageSize | Integer | 否 | 10 | 每页条数 |
| type | String | 否 | — | 角色过滤：`"buyer"`=我买到的；`"seller"`=我卖出的；**不传**=买卖全部（OR 查询） |

- **响应**：`Result<Page<MarketOrderVO>>`，`data.records` 为订单数组。

---

## 四、前端对接注意事项汇总

1. **鉴权**：订单相关接口及发布/下架商品均需登录态（服务端通过 SecurityUtil 获取当前用户），请求需携带 token；未登录返回 401。
2. **⚠️ `type` 取值**：是 `"buyer"` / `"seller"`，不是 `"buy"` / `"sell"`（已从 `MarketOrderServiceImpl` 实现确认）。
3. **`imageUrls` 是字符串不是数组**：数据库以 JSON 数组格式的 TEXT 存储，前端读取时需 `JSON.parse`，提交时序列化回字符串。
4. **订单操作使用 `orderSn`（字符串），不是数字 `id`**。
5. **商品状态联动**：列表/详情页应根据 status 控制按钮展示——status=0 显示"购买"，1 显示"锁定中"（不可购买），2 显示"已售出"，3 不展示或标记"已下架"；卖家仅对 status=0 的商品显示"下架"。
6. **错误处理**：统一判断 `code !== 200` 时用 `msg` 提示用户；创建订单接口需处理并发失败（商品被抢）的场景。
