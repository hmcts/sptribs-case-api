resource "azurerm_key_vault_secret" "app_insights_connection_string" {
  name         = "app-insights-connection-string"
  value        = data.azurerm_application_insights.ai.connection_string
  key_vault_id = data.azurerm_key_vault.key_vault.id

  content_type = "terraform-managed"
  tags = merge(var.common_tags, {
    "source" : "app insights ${data.azurerm_application_insights.ai.name}"
  })
}
