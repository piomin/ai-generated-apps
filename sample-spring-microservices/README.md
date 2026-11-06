### Driver Service

- `POST /api/drivers/register` - Register new driver (Public)
- `POST /api/drivers/{driverId}/location` - Update driver location (Authenticated)
- `PUT /api/drivers/{driverId}/active?active=true` - Set driver active status (Authenticated)
- `GET /api/drivers/active` - Get all active drivers (Authenticated)
- `GET /api/drivers/{driverId}` - Get driver details (Authenticated)
- `GET /api/drivers/nearby?latitude={lat}&longitude={lon}&radiusKm={r}&limit={n}` - Get nearby active drivers (Authenticated)

### Trip Service