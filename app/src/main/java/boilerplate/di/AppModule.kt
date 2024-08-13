package boilerplate.di

import boilerplate.data.remote.api.middleware.BooleanAdapter
import boilerplate.data.remote.api.middleware.DoubleAdapter
import boilerplate.data.remote.api.middleware.IntegerAdapter
import boilerplate.data.remote.api.middleware.StringAdapter
import boilerplate.utils.extension.BaseSchedulerProvider
import boilerplate.utils.extension.SchedulerProvider
import com.google.gson.FieldNamingPolicy
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.Strictness
import org.koin.dsl.module

val appModule = module {
	single { provideBaseSchedulerProvider() }
	single { provideGson() }
}

fun provideBaseSchedulerProvider(): BaseSchedulerProvider {
	return SchedulerProvider()
}

fun provideGson(): Gson {
	val booleanAdapter = BooleanAdapter()
	val integerAdapter = IntegerAdapter()
	val doubleAdapter = DoubleAdapter()
	val stringAdapter = StringAdapter()
	return GsonBuilder()
		.registerTypeAdapter(Boolean::class.java, booleanAdapter)
		.registerTypeAdapter(Int::class.java, integerAdapter)
		.registerTypeAdapter(Double::class.java, doubleAdapter)
		.registerTypeAdapter(String::class.java, stringAdapter)
		.setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
		.setStrictness(Strictness.LENIENT)
		.disableHtmlEscaping()
		.create()
}