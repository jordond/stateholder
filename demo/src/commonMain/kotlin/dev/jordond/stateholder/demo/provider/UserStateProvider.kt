package dev.jordond.stateholder.demo.provider

import dev.jordond.stateholder.demo.data.User
import dev.jordond.stateholder.demo.data.UserRepository
import dev.stateholder.provider.FlowStateProvider
import dev.stateholder.provider.flowStateProvider

class UserStateProvider(
    userRepository: UserRepository,
) : FlowStateProvider<User?> by flowStateProvider(
    flow = userRepository.currentUser,
)
