# 🎵 Configuración de Autenticación Spotify

Este documento explica cómo funciona el nuevo flujo de autenticación OAuth de Spotify implementado en la app.

## 🔄 Flujo de Autenticación

La app ahora implementa el flujo completo de autenticación OAuth de Spotify:

1. **Autenticación OAuth**: El usuario debe autorizar la app en Spotify
2. **Conexión App Remote**: Una vez autenticado, se conecta al SDK de App Remote
3. **Reproducción**: Ahora se puede controlar la reproducción de Spotify

## 📱 Experiencia del Usuario

1. Al abrir la app, se muestra la pantalla de conexión de Spotify
2. Se abre una webview/browser con el login de Spotify
3. El usuario acepta los permisos solicitados
4. La app recibe el token de acceso y se conecta automáticamente
5. Se redirige a la pantalla principal de la app

## 🔧 Configuración Técnica

### Credenciales Requeridas

En `local.properties`:
```properties
SPOTIFY_CLIENT_ID=tu_client_id_aqui
SPOTIFY_CLIENT_SECRET=tu_client_secret_aqui
SIMULATE_SPOTIFY_SUCCESS=false
```

### Redirect URI

La app está configurada para usar:
- **Redirect URI**: `mica-music://callback`

Este URI debe estar registrado en tu app de Spotify Developer Dashboard.

### Permisos Solicitados

La app solicita los siguientes scopes de Spotify:
- `app-remote-control`: Control remoto de la app
- `user-modify-playback-state`: Modificar el estado de reproducción
- `user-read-playback-state`: Leer el estado de reproducción

## 🚀 Cómo Usar

### Para Desarrollo
1. Asegúrate de tener las credenciales correctas en `local.properties`
2. Verifica que el redirect URI esté registrado en Spotify
3. Compila y ejecuta la app
4. El flujo de auth se iniciará automáticamente

### Para Testing con Simulación
Para saltarse la autenticación durante desarrollo:
```properties
SIMULATE_SPOTIFY_SUCCESS=true
```

## 🛠️ Implementación Técnica

### Archivos Modificados

1. **SpotifyService.kt**: 
   - Añadido soporte para OAuth authentication
   - Nuevos métodos `startAuth()` y `handleAuthResponse()`
   - Verificación de autenticación antes de conectar App Remote

2. **SpotifyConnectionActivity.kt**:
   - Implementa el flujo completo auth → connection
   - Maneja callbacks de autenticación y conexión
   - Mejorada la UX con mensajes informativos

### Nuevas Interfaces

```kotlin
interface SpotifyAuthListener {
    fun onAuthSuccess(accessToken: String)
    fun onAuthError(error: String)
    fun onAuthCancelled()
}
```

## 🔍 Troubleshooting

### Error: "Explicit user authorization is required"
✅ **Solucionado**: Este error aparecía porque la app intentaba conectar App Remote sin autenticación OAuth previa. Ahora se autentica primero.

### Error: "Invalid client"
- Verifica que el `SPOTIFY_CLIENT_ID` sea correcto
- Asegúrate de que la app esté registrada en Spotify Developer Dashboard

### Error: "Invalid redirect URI"
- Verifica que `mica-music://callback` esté registrado en tu app de Spotify
- Asegúrate de que el scheme y host coincidan exactamente

### La webview no aparece
- Verifica que tengas la app de Spotify instalada
- En algunos casos, se abrirá el browser por defecto en lugar de una webview

## 📝 Notas Adicionales

- El token de acceso se guarda en memoria durante la sesión
- Si la app se cierra, será necesario autenticarse nuevamente
- Para implementación de producción, considera guardar el token de forma persistente (con renovación)
- Los tokens de Spotify tienen una duración limitada (1 hora típicamente)

## 🎯 Próximos Pasos

Para mejorar la experiencia:
1. **Persistencia de tokens**: Guardar tokens y manejar renovación automática
2. **Manejo de errores mejorado**: Mensajes más específicos para diferentes tipos de error
3. **Retry automático**: Reintentar conexión automáticamente en caso de fallo temporal
4. **Estado de conexión**: Indicador visual del estado de conexión en la UI principal
