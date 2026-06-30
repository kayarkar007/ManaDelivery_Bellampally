package com.example.manadeliverybellempally

import android.app.Activity
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import com.example.manadeliverybellempally.data.model.UserRole
import com.example.manadeliverybellempally.data.repository.AuthState
import com.example.manadeliverybellempally.ui.auth.*
import com.example.manadeliverybellempally.ui.customer.*
import com.example.manadeliverybellempally.ui.vendor.*
import com.example.manadeliverybellempally.ui.rider.*
import com.example.manadeliverybellempally.ui.admin.*

@Composable
fun MainNavigation() {
    val backStack = rememberNavBackStack(Splash)
    val authViewModel: AuthViewModel = viewModel()
    val customerViewModel: CustomerViewModel = viewModel()
    val authState by authViewModel.authState.collectAsState()
    val context = LocalContext.current
    val activity = context as? Activity

    var phoneNumber by remember { mutableStateOf("") }
    var currentRole by remember { mutableStateOf(UserRole.CUSTOMER) }
    var userName by remember { mutableStateOf("") }

    NavDisplay(
        backStack = backStack,
        onBack = { backStack.removeLastOrNull() },
        entryProvider = entryProvider {
            entry<Splash> {
                SplashScreen(
                    authState = authState,
                    viewModel = authViewModel,
                    onNavigateToLogin = {
                        backStack.clear()
                        backStack.add(Login)
                    },
                    onNavigateToHome = { role ->
                        currentRole = role
                        backStack.clear()
                        when (currentRole) {
                            UserRole.CUSTOMER -> backStack.add(CustomerHome)
                            UserRole.VENDOR -> backStack.add(VendorHome)
                            UserRole.RIDER -> backStack.add(RiderHome)
                            UserRole.ADMIN -> backStack.add(AdminHome)
                        }
                    },
                )
            }

            entry<Login> {
                LaunchedEffect(authState) {
                    if (authState is AuthState.CodeSent) backStack.add(OtpVerify)
                }
                LoginScreen(
                    authState = authState,
                    onSendOtp = { phone ->
                        phoneNumber = phone
                        activity?.let { authViewModel.sendOtp(phone, it) }
                    },
                    onNavigateToSignup = { backStack.add(Signup) },
                )
            }

            entry<OtpVerify> {
                LaunchedEffect(authState) {
                    if (authState is AuthState.Authenticated) {
                        val auth = authState as AuthState.Authenticated
                        val role = authViewModel.syncUserToDatabaseAndGetRole(auth.uid, auth.phone, currentRole)
                        backStack.clear()
                        when (role) {
                            UserRole.CUSTOMER -> backStack.add(CustomerHome)
                            UserRole.VENDOR -> backStack.add(VendorHome)
                            UserRole.RIDER -> backStack.add(RiderHome)
                            UserRole.ADMIN -> backStack.add(AdminHome)
                        }
                    }
                }
                OtpScreen(
                    phoneNumber = phoneNumber,
                    authState = authState,
                    onVerifyOtp = { authViewModel.verifyOtp(it) },
                    onResendOtp = { activity?.let { authViewModel.sendOtp(phoneNumber, it) } },
                    onBack = { authViewModel.resetState(); backStack.removeLastOrNull() },
                )
            }

            entry<Signup> {
                LaunchedEffect(authState) {
                    if (authState is AuthState.CodeSent) backStack.add(OtpVerify)
                }
                SignupScreen(
                    authState = authState,
                    onSignup = { name, role, phone ->
                        userName = name
                        currentRole = role
                        phoneNumber = phone
                        authViewModel.setUserName(name)
                        activity?.let { authViewModel.sendOtp(phone, it) }
                    },
                    onNavigateToLogin = { authViewModel.resetState(); backStack.removeLastOrNull() },
                )
            }

            // ─── Customer ───
            entry<CustomerHome> {
                val currentUid = (authState as? AuthState.Authenticated)?.uid ?: ""
                CustomerHomeScreen(
                    customerId = currentUid,
                    viewModel = customerViewModel,
                    onCategoryClick = { backStack.add(CategoryDetail(it)) },
                    onVendorClick = { backStack.add(VendorStore(it)) },
                    onSearchClick = { backStack.add(Search) },
                    onCartClick = { backStack.add(Checkout) },
                    onOrdersClick = { backStack.add(CustomerOrders) },
                    onProfileClick = { backStack.add(ProfileScreen) },
                    onWalletClick = { backStack.add(WalletScreen) },
                )
            }

            entry<ProfileScreen> {
                ProfileScreen(
                    viewModel = customerViewModel,
                    onAddressClick = { backStack.add(AddressManager) },
                    onOrdersClick = { backStack.add(CustomerOrders) },
                    onWalletClick = { backStack.add(WalletScreen) },
                    onSupportClick = { backStack.add(CustomerSupport(null)) },
                    onLogout = { authViewModel.signOut(); backStack.clear(); backStack.add(Login) },
                    onBack = { backStack.removeLastOrNull() }
                )
            }

            entry<CategoryDetail> { screen ->
                CategoryDetailScreen(
                    categoryId = screen.categoryId,
                    viewModel = customerViewModel,
                    onVendorClick = { backStack.add(VendorStore(it)) },
                    onBack = { backStack.removeLastOrNull() }
                )
            }

            entry<VendorStore> { screen ->
                VendorStoreScreen(
                    vendorId = screen.vendorId,
                    viewModel = customerViewModel,
                    onBack = { backStack.removeLastOrNull() },
                    onCheckout = { backStack.add(Checkout) }
                )
            }

            entry<Checkout> {
                CheckoutScreen(
                    viewModel = customerViewModel,
                    onBack = { backStack.removeLastOrNull() },
                    onOrderPlaced = {
                        backStack.removeLastOrNull()
                        while (backStack.lastOrNull() !is CustomerHome) { backStack.removeLastOrNull() }
                        backStack.add(CustomerOrders)
                    }
                )
            }

            entry<CustomerOrders> {
                CustomerOrdersScreen(
                    viewModel = customerViewModel,
                    onOrderClick = { backStack.add(OrderTracking(it)) },
                    onVendorClick = { backStack.add(VendorStore(it)) },
                    onBack = { backStack.removeLastOrNull() }
                )
            }

            entry<OrderTracking> { screen ->
                OrderTrackingScreen(
                    orderId = screen.orderId,
                    viewModel = customerViewModel,
                    onBack = { backStack.removeLastOrNull() },
                    onSupportClick = { id -> backStack.add(CustomerSupport(id)) }
                )
            }
            
            entry<CustomerSupport> { screen ->
                SupportScreen(
                    orderId = screen.orderId,
                    viewModel = customerViewModel,
                    onBack = { backStack.removeLastOrNull() },
                    onSubmitSuccess = { backStack.removeLastOrNull() }
                )
            }

            entry<AddressManager> {
                AddressManagerScreen(viewModel = customerViewModel, onBack = { backStack.removeLastOrNull() })
            }

            entry<WalletScreen> {
                WalletScreen(viewModel = customerViewModel, onBack = { backStack.removeLastOrNull() })
            }

            entry<Search> {
                SearchScreen(
                    viewModel = customerViewModel,
                    onVendorClick = { backStack.add(VendorStore(it)) },
                    onBack = { backStack.removeLastOrNull() }
                )
            }

            // ─── Vendor ───
            entry<VendorHome> {
                val currentUid = (authState as? AuthState.Authenticated)?.uid ?: ""
                VendorDashboardScreen(
                    vendorId = currentUid,
                    onLogout = { authViewModel.signOut(); backStack.clear(); backStack.add(Login) },
                    viewModel = viewModel()
                )
            }

            entry<VendorSettings> {
                VendorProfileScreen(viewModel = viewModel(), onBack = { backStack.removeLastOrNull() })
            }

            // ─── Rider ───
            entry<RiderHome> {
                val currentUid = (authState as? AuthState.Authenticated)?.uid ?: ""
                RiderDashboardScreen(
                    riderId = currentUid,
                    onLogout = { authViewModel.signOut(); backStack.clear(); backStack.add(Login) },
                    viewModel = viewModel()
                )
            }

            entry<RiderCompliance> {
                RiderKYCScreen(viewModel = viewModel(), onBack = { backStack.removeLastOrNull() })
            }

            // ─── Admin ───
            entry<AdminHome> {
                val adminViewModel: AdminViewModel = viewModel()
                AdminDashboardScreen(
                    viewModel = adminViewModel,
                    onLogout = { authViewModel.signOut(); backStack.clear(); backStack.add(Login) },
                    onOrdersClick = { backStack.add(AdminOrderDetail(it)) },
                    onUsersClick = { backStack.add(AdminUserDetail("")) },
                    onVendorsClick = { backStack.add(AdminVendorDetail("")) },
                    onSettingsClick = { backStack.add(AdminGlobalSettings) },
                    onBroadcastClick = { backStack.add(AdminBroadcast) },
                    onSupportClick = { backStack.add(AdminSupport) }
                )
            }

            entry<AdminOrderDetail> { screen ->
                AdminOrderDetailScreen(
                    orderId = screen.orderId,
                    viewModel = viewModel<AdminViewModel>(),
                    onBack = { backStack.removeLastOrNull() }
                )
            }

            entry<AdminUserDetail> {
                AdminUserListScreen(viewModel = viewModel<AdminViewModel>(), onBack = { backStack.removeLastOrNull() })
            }

            entry<AdminVendorDetail> {
                AdminVendorListScreen(viewModel = viewModel<AdminViewModel>(), onBack = { backStack.removeLastOrNull() })
            }

            entry<AdminCouponManager> {
                AdminCouponManagerScreen(viewModel = viewModel<AdminViewModel>(), onBack = { backStack.removeLastOrNull() })
            }

            entry<AdminBannerManager> {
                AdminBannerManagerScreen(viewModel = viewModel<AdminViewModel>(), onBack = { backStack.removeLastOrNull() })
            }

            entry<AdminGlobalSettings> {
                AdminSettingsScreen(viewModel = viewModel<AdminViewModel>(), onBack = { backStack.removeLastOrNull() })
            }

            entry<OrderHistory> {
                AdminPayoutScreen(viewModel = viewModel<AdminViewModel>(), onBack = { backStack.removeLastOrNull() })
            }

            entry<AdminReports> {
                AdminAnalyticsScreen(viewModel = viewModel<AdminViewModel>(), onBack = { backStack.removeLastOrNull() })
            }

            entry<AdminAuditLogs> {
                AdminAuditLogsScreen(onBack = { backStack.removeLastOrNull() })
            }

            entry<AdminBroadcast> {
                AdminBroadcastScreen(onBack = { backStack.removeLastOrNull() })
            }

            entry<AdminSupport> {
                AdminSupportScreen(viewModel = viewModel<AdminViewModel>(), onBack = { backStack.removeLastOrNull() })
            }
        },
    )
}
