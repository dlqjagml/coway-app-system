package assignment.cowaysystem.feature.order.service

import assignment.cowaysystem.common.exception.BadRequestException
import assignment.cowaysystem.feature.order.dto.OrderList
import assignment.cowaysystem.feature.order.dto.OrderReq
import assignment.cowaysystem.feature.order.entity.Delivery
import assignment.cowaysystem.feature.order.entity.Order
import assignment.cowaysystem.feature.order.entity.OrderItem
import assignment.cowaysystem.feature.order.repository.ItemRepository
import assignment.cowaysystem.feature.order.repository.MemberRepository
import assignment.cowaysystem.feature.order.repository.OrderRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional(readOnly = true)
class OrderService(
        private val orderRepository: OrderRepository,
        private val itemRepository: ItemRepository,
        private val memberRepository: MemberRepository
){

    /**
     * 주문 기능
     * 1. 주문한 아이템이 리스트 형식으로 가능하게 하기 (o)
     * 2. 주문한 상품에 대해 주기적 방문 서비스 이용 여부 확인하는 코드 작성 필요
     */
    @Transactional
    fun order(
            loginId: String,
            orderReq: List<OrderReq>
    ): Order{
        /**
         * 주문한 사용자 데이터 베이스에 있는지 확인
         */
        val orderMember = memberRepository.findByLoginId(loginId)
                ?: throw BadRequestException("주문하려는 ${loginId}가 존재하지 않습니다.")

        /**
         * 주문한 상품의 리스트
         */
        val orderList = orderReq.map {
            OrderList(
                    itemRepository.findByName(it.itemName)?: throw BadRequestException("주문하려는 ${it.itemName}가 존재하지 않습니다."),
                    it.count,
                    it.color
            )
        }

        /**
         * 배송정보 -> 회원의 정보로 기입
         */
        val delivery = Delivery().also {
            it.address = orderMember.address
        }

        /**
         * orderItems 생성 (아이템 수량 관리)
         */
        val orderItems = orderList.map {
            OrderItem().createOrderItem(
                    it.item,
                    it.count,
                    it.color
            )
        }

        /**
         * 주문 생성
         * 1. orderItems 바탕으로 주문생성
         * 2. member 바탕으로 주문 회원 확인
         * 3. member 주소를 바탕으로 delivery 생성
         */
        val createdOrder = Order().createOrder(
                member = orderMember,
                delivery = delivery,
                orderItems = orderItems
        )

        return orderRepository.save(createdOrder)
    }

}