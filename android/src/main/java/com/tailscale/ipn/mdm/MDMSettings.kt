// Copyright (c) Tailscale Inc & AUTHORS
// SPDX-License-Identifier: BSD-3-Clause

package com.tailscale.ipn.mdm

import android.content.RestrictionsManager
import android.content.SharedPreferences
import com.tailscale.ipn.App
import kotlin.reflect.KVisibility
import kotlin.reflect.full.declaredMemberProperties
import kotlin.reflect.full.isSubclassOf
import kotlin.reflect.jvm.jvmErasure

object MDMSettings {
  // The String message used in this NoSuchKeyException must match the value of
  // syspolicy.ErrNoSuchKey defined in Go. We compare against its exact text
  // to determine whether the requested policy setting is not configured and
  // an actual syspolicy.ErrNoSuchKey should be returned from syspolicyHandler
  // to the backend.
  class NoSuchKeyException : Exception("no such key")

  // MDM restriction keys
  const val KEY_HARDWARE_ATTESTATION = "HardwareAttestation"

  // We default this to true, so that stricter behavior is used during initialization,
  // prior to receiving MDM restrictions.
  var isMDMConfigured = true
    private set

  val forceEnabled = BooleanMDMSetting("ForceEnabled", "Alternar Conexão Forçada")

  // Handled on the backed
  val exitNodeID = StringMDMSetting("ExitNodeID", "Nó de Saída Forçado: ID Estável")

  // (jonathan) TODO: Unused but required. There is some funky go string duration parsing required
  // here.
  val keyExpirationNotice = StringMDMSetting("KeyExpirationNotice", "Período de Aviso de Expiração da Chave")

  val loginURL = StringMDMSetting("LoginURL", "URL personalizada do servidor de controle")

  val managedByCaption = StringMDMSetting("ManagedByCaption", "Gerenciado Por - Legenda")

  val managedByOrganizationName =
      StringMDMSetting("ManagedByOrganizationName", "Gerenciado Por - Nome da Organização")

  val managedByURL = StringMDMSetting("ManagedByURL", "Gerenciado Por - URL de Suporte")

  // Handled on the backend
  val tailnet = StringMDMSetting("Tailnet", "Nome da Rede Recomendada/Obrigatória")

  val hiddenNetworkDevices =
      StringArrayListMDMSetting("HiddenNetworkDevices", "Categorias de Dispositivos de Rede Ocultos")

  // Unused on Android
  val allowIncomingConnections =
      AlwaysNeverUserDecidesMDMSetting("AllowIncomingConnections", "Permitir Conexões de Entrada")

  // Unused on Android
  val detectThirdPartyAppConflicts =
      AlwaysNeverUserDecidesMDMSetting(
          "DetectThirdPartyAppConflicts", "Detectar aplicativos de terceiros potencialmente problemáticos")

  val exitNodeAllowLANAccess =
      AlwaysNeverUserDecidesMDMSetting(
          "ExitNodeAllowLANAccess", "Permitir acesso à LAN ao usar um nó de saída")

  // Handled on the backend
  val postureChecking =
      AlwaysNeverUserDecidesMDMSetting("PostureChecking", "Ativar Verificação de Postura")

  // Handled on the backend
  val deviceSerialNumber =
      StringMDMSetting(
          "DeviceSerialNumber", "Número de série do dispositivo que está executando o Angolan VPN")

  val useTailscaleDNSSettings =
      AlwaysNeverUserDecidesMDMSetting("UseTailscaleDNSSettings", "Usar Configurações de DNS do Angolan VPN")

  // Unused on Android
  val useTailscaleSubnets =
      AlwaysNeverUserDecidesMDMSetting("UseTailscaleSubnets", "Usar Sub-redes do Angolan VPN")

  val exitNodesPicker = ShowHideMDMSetting("ExitNodesPicker", "Seletor de Nós de Saída")

  val manageTailnetLock = ShowHideMDMSetting("ManageTailnetLock", "Item de menu “Gerenciar bloqueio de rede”")

  // Unused on Android
  val resetToDefaults = ShowHideMDMSetting("ResetToDefaults", "Item de menu “Redefinir para Padrões”")

  val runExitNode = ShowHideMDMSetting("RunExitNode", "Executar como Nó de Saída")

  // Unused on Android
  val testMenu = ShowHideMDMSetting("TestMenu", "Mostrar Menu de Depuração")

  // Unused on Android
  val updateMenu = ShowHideMDMSetting("UpdateMenu", "Item de menu “Atualização Disponível”")

  // (jonathan) TODO: Use this when suggested exit nodes are implemented
  val allowedSuggestedExitNodes =
      StringArrayListMDMSetting("AllowedSuggestedExitNodes", "Nós de Saída Sugeridos Permitidos")

  // Allows admins to define a list of packages that won't be routed via Tailscale.
  val excludedPackages = StringMDMSetting("ExcludedPackageNames", "Nomes de Pacotes Excluídos")
  // Allows admins to define a list of packages that will be routed via Tailscale, letting all other
  // apps skip the VPN tunnel.
  val includedPackages = StringMDMSetting("IncludedPackageNames", "Nomes de Pacotes Incluídos")

  // Handled on the backend
  val authKey = StringMDMSetting("AuthKey", "Chave de Autenticação para login")

  // Overrides the value provided by os.Hostname() in Go
  val hostname = StringMDMSetting("Hostname", "Nome de Host do Dispositivo")

  // Allows admins to skip the get started intro screen
  val onboardingFlow = ShowHideMDMSetting("OnboardingFlow", "Ocultar a tela de introdução")

  val allSettings by lazy {
    MDMSettings::class
        .declaredMemberProperties
        .filter {
          it.visibility == KVisibility.PUBLIC &&
              it.returnType.jvmErasure.isSubclassOf(MDMSetting::class)
        }
        .map { it.call(MDMSettings) as MDMSetting<*> }
  }

  val hardwareAttestation =
      BooleanMDMSetting(
          KEY_HARDWARE_ATTESTATION,
          "Usar chaves protegidas por hardware para vincular a identidade do nó ao dispositivo",
      )

  val allSettingsByKey by lazy { allSettings.associateBy { it.key } }

  fun loadFrom(preferences: Lazy<SharedPreferences>, restrictionsManager: RestrictionsManager?) {
    val bundle = restrictionsManager?.applicationRestrictions
    allSettings.forEach { it.setFrom(bundle, preferences) }
    isMDMConfigured = bundle != null && !bundle.isEmpty
  }

  fun update(app: App, restrictionsManager: RestrictionsManager?) {
    loadFrom(lazy { app.getEncryptedPrefs() }, restrictionsManager)
    app.notifyPolicyChanged()
  }
}
